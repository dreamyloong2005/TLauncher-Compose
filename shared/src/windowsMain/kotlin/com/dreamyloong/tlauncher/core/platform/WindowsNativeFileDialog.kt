package com.dreamyloong.tlauncher.core.platform

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.WString
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.COM.Unknown
import com.sun.jna.platform.win32.Guid
import com.sun.jna.platform.win32.Ole32
import com.sun.jna.platform.win32.WTypes
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions
import java.awt.KeyboardFocusManager
import java.awt.Window
import java.io.File

internal data class WindowsFileDialogFilter(
    val name: String,
    val spec: String,
)

internal object WindowsNativeFileDialog {
    private val filePickerClientGuid = Guid.GUID("{7B34CE88-0819-4F3D-8FA2-95F78420B1D1}")
    private val extensionPackagePickerClientGuid = Guid.GUID("{4A7E9F72-7D7A-4D67-9C1C-8C359D1F7B58}")
    private val directoryPickerClientGuid = Guid.GUID("{DB9246A7-46E2-47E0-B3BC-35AB71723C8D}")

    fun pickFile(title: String, filters: List<WindowsFileDialogFilter>): File? {
        val ownerHwnd = currentOwnerHwnd()
        return runDialog {
            pickFileSystemPath(
                title = title,
                filters = filters,
                clientGuid = filePickerClientGuid,
                pickFolders = false,
                defaultFolderPath = null,
                ownerHwnd = ownerHwnd,
            )?.let(::File)?.takeIf(File::isFile)
        }
    }

    fun pickExtensionPackage(): File? {
        val ownerHwnd = currentOwnerHwnd()
        return runDialog {
            pickFileSystemPath(
                title = "Load .textension Extension",
                filters = listOf(WindowsFileDialogFilter("TLauncher Extension (*.textension)", "*.textension")),
                clientGuid = extensionPackagePickerClientGuid,
                pickFolders = false,
                defaultFolderPath = null,
                ownerHwnd = ownerHwnd,
            )?.let(::File)?.takeIf(File::isFile)
        }
    }

    fun pickDirectory(initialPath: String?): String? {
        val ownerHwnd = currentOwnerHwnd()
        return runDialog {
            pickFileSystemPath(
                title = "Choose Folder",
                filters = emptyList(),
                clientGuid = directoryPickerClientGuid,
                pickFolders = true,
                defaultFolderPath = initialPath?.takeIf { path -> path.isNotBlank() },
                ownerHwnd = ownerHwnd,
            )?.let(::File)
                ?.takeIf(File::isDirectory)
                ?.absolutePath
        }
    }

    private fun <T> runDialog(block: () -> T?): T? {
        return runCatching {
            val initializeResult = Ole32.INSTANCE.OleInitialize(Pointer.NULL)
            when (hresultCode(initializeResult)) {
                COMUtils.S_OK,
                COMUtils.S_FALSE,
                -> try {
                    block()
                } finally {
                    Ole32.INSTANCE.OleUninitialize()
                }
                HRESULT_RPC_E_CHANGED_MODE -> runDialogThread(block)
                else -> null
            }
        }.onFailure { throwable ->
            throwable.printStackTrace()
        }.getOrNull()
    }

    private fun pickFileSystemPath(
        title: String,
        filters: List<WindowsFileDialogFilter>,
        clientGuid: Guid.GUID,
        pickFolders: Boolean,
        defaultFolderPath: String?,
        ownerHwnd: WinDef.HWND?,
    ): String? {
        val dialogRef = PointerByReference()
        val createResult = Ole32.INSTANCE.CoCreateInstance(
            CLSID_FILE_OPEN_DIALOG,
            null,
            WTypes.CLSCTX_INPROC_SERVER,
            IID_FILE_OPEN_DIALOG,
            dialogRef,
        )
        if (COMUtils.FAILED(createResult)) {
            return null
        }

        val dialog = WindowsFileOpenDialog(dialogRef.value)
        try {
            dialog.setClientGuid(clientGuid)
            dialog.setTitle(title)
            dialog.configureOptions(pickFolders)
            dialog.setFilters(filters)
            defaultFolderPath?.let { path ->
                createShellItem(path)?.use { item ->
                    dialog.setDefaultFolder(item)
                }
            }
            if (!dialog.show(ownerHwnd)) {
                return null
            }
            return dialog.resultFileSystemPath()
        } finally {
            dialog.Release()
        }
    }

    private fun <T> runDialogThread(block: () -> T?): T? {
        var result: Result<T?>? = null
        val thread = Thread {
            result = runCatching {
                val initializeResult = Ole32.INSTANCE.OleInitialize(Pointer.NULL)
                val shouldUninitialize = hresultCode(initializeResult) == COMUtils.S_OK ||
                    hresultCode(initializeResult) == COMUtils.S_FALSE
                try {
                    if (COMUtils.FAILED(initializeResult)) {
                        null
                    } else {
                        block()
                    }
                } finally {
                    if (shouldUninitialize) {
                        Ole32.INSTANCE.OleUninitialize()
                    }
                }
            }
        }.apply {
            name = "TLauncher-WindowsFileDialog"
            isDaemon = true
        }
        thread.start()
        return try {
            thread.join()
            result?.onFailure { throwable ->
                throwable.printStackTrace()
            }?.getOrNull()
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            null
        }
    }

    private fun createShellItem(path: String): ShellItemHandle? {
        val directory = File(path).takeIf(File::isDirectory) ?: return null
        val itemRef = PointerByReference()
        val result = Shell32Dialog.INSTANCE.SHCreateItemFromParsingName(
            WString(directory.absolutePath),
            null,
            Guid.REFIID(IID_SHELL_ITEM),
            itemRef,
        )
        if (COMUtils.FAILED(result)) {
            return null
        }
        return ShellItemHandle(ShellItem(itemRef.value))
    }

    private fun currentOwnerHwnd(): WinDef.HWND? {
        val window = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow
            ?: KeyboardFocusManager.getCurrentKeyboardFocusManager().focusedWindow
            ?: Window.getWindows().firstOrNull { candidate -> candidate.isActive || candidate.isFocused }
            ?: Window.getWindows().firstOrNull { candidate -> candidate.isShowing }

        return window?.let { activeWindow ->
            runCatching {
                Native.getWindowPointer(activeWindow)
                    ?.takeIf { pointer -> Pointer.nativeValue(pointer) != 0L }
                    ?.let { pointer -> WinDef.HWND(pointer) }
            }.getOrNull()
        }
    }
}

private class WindowsFileOpenDialog(pointer: Pointer) : Unknown(pointer) {
    fun setClientGuid(guid: Guid.GUID) {
        val guidRef = Guid.GUID.ByReference(guid).apply { write() }
        invokeHResult(VTABLE_SET_CLIENT_GUID, guidRef)
    }

    fun setTitle(title: String) {
        if (title.isNotBlank()) {
            invokeHResult(VTABLE_SET_TITLE, WString(title))
        }
    }

    fun configureOptions(pickFolders: Boolean) {
        val options = IntByReference()
        val getResult = invokeHResult(VTABLE_GET_OPTIONS, options)
        if (COMUtils.FAILED(getResult)) {
            return
        }
        var nextOptions = options.value or FOS_FORCEFILESYSTEM or FOS_PATHMUSTEXIST
        nextOptions = if (pickFolders) {
            nextOptions or FOS_PICKFOLDERS
        } else {
            nextOptions or FOS_FILEMUSTEXIST
        }
        invokeHResult(VTABLE_SET_OPTIONS, nextOptions)
    }

    fun setFilters(filters: List<WindowsFileDialogFilter>) {
        if (filters.isEmpty()) {
            return
        }
        @Suppress("UNCHECKED_CAST")
        val specs = NativeFilterSpec().toArray(filters.size) as Array<NativeFilterSpec>
        filters.forEachIndexed { index, filter ->
            specs[index].pszName = WString(filter.name)
            specs[index].pszSpec = WString(filter.spec)
            specs[index].write()
        }
        val result = invokeHResult(VTABLE_SET_FILE_TYPES, filters.size, specs.first().pointer)
        if (COMUtils.SUCCEEDED(result)) {
            invokeHResult(VTABLE_SET_FILE_TYPE_INDEX, 1)
        }
    }

    fun setDefaultFolder(item: ShellItemHandle) {
        invokeHResult(VTABLE_SET_DEFAULT_FOLDER, item.pointer)
    }

    fun show(ownerHwnd: WinDef.HWND?): Boolean {
        val result = invokeHResult(VTABLE_SHOW, ownerHwnd)
        return when (hresultCode(result)) {
            HRESULT_ERROR_CANCELLED -> false
            else -> COMUtils.SUCCEEDED(result)
        }
    }

    fun resultFileSystemPath(): String? {
        val itemRef = PointerByReference()
        val result = invokeHResult(VTABLE_GET_RESULT, itemRef)
        if (COMUtils.FAILED(result)) {
            return null
        }
        return ShellItemHandle(ShellItem(itemRef.value)).use { item ->
            item.fileSystemPath()
        }
    }

    private fun invokeHResult(vtableId: Int, vararg args: Any?): WinNT.HRESULT {
        return _invokeNativeObject(
            vtableId,
            arrayOf(pointer, *args),
            WinNT.HRESULT::class.java,
        ) as WinNT.HRESULT
    }
}

private class ShellItem(pointer: Pointer) : Unknown(pointer) {
    fun fileSystemPath(): String? {
        val nameRef = PointerByReference()
        val result = _invokeNativeObject(
            VTABLE_SHELL_ITEM_GET_DISPLAY_NAME,
            arrayOf(pointer, SIGDN_FILESYSPATH, nameRef),
            WinNT.HRESULT::class.java,
        ) as WinNT.HRESULT
        if (COMUtils.FAILED(result)) {
            return null
        }
        val namePointer = nameRef.value ?: return null
        return try {
            namePointer.getWideString(0)
        } finally {
            Ole32.INSTANCE.CoTaskMemFree(namePointer)
        }
    }
}

private class ShellItemHandle(private val item: ShellItem) : AutoCloseable {
    val pointer: Pointer
        get() = item.pointer

    fun fileSystemPath(): String? = item.fileSystemPath()

    override fun close() {
        item.Release()
    }
}

@Structure.FieldOrder("pszName", "pszSpec")
internal class NativeFilterSpec : Structure() {
    @JvmField
    var pszName: WString? = null

    @JvmField
    var pszSpec: WString? = null
}

private interface Shell32Dialog : StdCallLibrary {
    fun SHCreateItemFromParsingName(
        path: WString,
        bindContext: Pointer?,
        riid: Guid.REFIID,
        item: PointerByReference,
    ): WinNT.HRESULT

    companion object {
        val INSTANCE: Shell32Dialog = Native.load(
            "shell32",
            Shell32Dialog::class.java,
            W32APIOptions.UNICODE_OPTIONS,
        )
    }
}

private fun hresultCode(result: WinNT.HRESULT): Int = result.toInt()

private val CLSID_FILE_OPEN_DIALOG = Guid.CLSID("{DC1C5A9C-E88A-4DDE-A5A1-60F82A20AEF7}")
private val IID_FILE_OPEN_DIALOG = Guid.IID("{D57C7288-D4AD-4768-BE02-9D969532D960}")
private val IID_SHELL_ITEM = Guid.IID("{43826D1E-E718-42EE-BC55-A1E261C37BFE}")

private const val HRESULT_ERROR_CANCELLED = -2147023673
private const val HRESULT_RPC_E_CHANGED_MODE = -2147417850
private const val SIGDN_FILESYSPATH = -2147123200

private const val FOS_PICKFOLDERS = 0x00000020
private const val FOS_FORCEFILESYSTEM = 0x00000040
private const val FOS_PATHMUSTEXIST = 0x00000800
private const val FOS_FILEMUSTEXIST = 0x00001000

private const val VTABLE_SHOW = 3
private const val VTABLE_SET_FILE_TYPES = 4
private const val VTABLE_SET_FILE_TYPE_INDEX = 5
private const val VTABLE_SET_OPTIONS = 9
private const val VTABLE_GET_OPTIONS = 10
private const val VTABLE_SET_DEFAULT_FOLDER = 11
private const val VTABLE_SET_TITLE = 17
private const val VTABLE_GET_RESULT = 20
private const val VTABLE_SET_CLIENT_GUID = 24
private const val VTABLE_SHELL_ITEM_GET_DISPLAY_NAME = 5
