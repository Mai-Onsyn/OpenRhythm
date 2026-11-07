@file:JvmName("WindowsOpenGLRedrawerKt")

package org.jetbrains.skiko.redrawer

import mai_onsyn.compose_gl.internal.desktop.skiko.SkikoGlShare

/**
 * skiko 0.144.6 里 `org.jetbrains.skiko.redrawer.WindowsOpenGLRedrawerKt` 的**影子类**。
 *
 * 原来的类里是 7 个 `private static native`，全部实现在 `skiko-windows-x64.dll` 里，
 * 其中 `createContext(device, hwnd, transparent)` 的签名**不带 shareContext**，
 * 导致外部无法把 Skiko 的 GL 上下文拉进我们的 Share Group（事后 `wglShareLists`
 * 会因上下文正在别的线程 current 而 `ERROR_BUSY`）。
 *
 * 这个同名类放在 classpath 更靠前的位置，顶掉原实现，改用 LWJGL 自己做同样的事，
 * 唯一的差别是创建上下文时传入我们的 bootstrap 上下文作为 share。
 *
 * 注意：签名必须与 `WindowsOpenGLRedrawer` 的实际调用完全一致（已用 javap 核对）：
 *
 * ```
 * getDevice(hwnd): Long
 * createContext(device, hwnd, transparent): Long
 * makeCurrent(device, context)
 * swapBuffers(device)
 * deleteContext(context)
 * setSwapInterval(interval)
 * dwmFlush()
 * ```
 */
fun `access$getDevice`(hwnd: Long): Long = SkikoGlShare.deviceFor(hwnd)

fun `access$createContext`(device: Long, hwnd: Long, transparent: Boolean): Long =
    SkikoGlShare.createContext(device, hwnd, transparent)

fun `access$makeCurrent`(device: Long, context: Long) = SkikoGlShare.makeCurrent(device, context)

fun `access$swapBuffers`(device: Long) = SkikoGlShare.swapBuffers(device)

fun `access$deleteContext`(context: Long) = SkikoGlShare.deleteContext(context)

fun `access$setSwapInterval`(interval: Int) = SkikoGlShare.setSwapInterval(interval)

fun `access$dwmFlush`() = SkikoGlShare.dwmFlush()
