package mai_onsyn.open_rhythm.ui.pages.setting.categories.key_map

import androidx.compose.ui.input.key.Key

data class KeyData(
    val code: Long,
    val firstName: String,
    val lastName: String? = null,
    val offsetX: Float,
    val offsetY: Float,
    val width: Float = 1f,
    val height: Float = 1f,
) {
    constructor(
        key: Key,
        firstName: String,
        offsetX: Float,
        offsetY: Float,
        width: Float = 1f,
        height: Float = 1f,
        lastName: String? = null,
    ): this(key.keyCode, firstName, lastName, offsetX, offsetY, width, height)
}

val mainArea = listOf(
    // ---- F 区 ----
    KeyData(Key.Escape, "ESC", 0f, 0f),
    KeyData(Key.F1, "F1", 2f, 0f),
    KeyData(Key.F2, "F2", 3f, 0f),
    KeyData(Key.F3, "F3", 4f, 0f),
    KeyData(Key.F4, "F4", 5f, 0f),
    KeyData(Key.F5, "F5", 6.5f, 0f),
    KeyData(Key.F6, "F6", 7.5f, 0f),
    KeyData(Key.F7, "F7", 8.5f, 0f),
    KeyData(Key.F8, "F8", 9.5f, 0f),
    KeyData(Key.F9, "F9", 11f, 0f),
    KeyData(Key.F10, "F10", 12f, 0f),
    KeyData(Key.F11, "F11", 13f, 0f),
    KeyData(Key.F12, "F12", 14f, 0f),

    // ---- 数字行 ----
    KeyData(Key.Grave, "`", 0f, 1.25f, lastName = "~"),
    KeyData(Key.One, "1", 1f, 1.25f, lastName = "!"),
    KeyData(Key.Two, "2", 2f, 1.25f, lastName = "@"),
    KeyData(Key.Three, "3", 3f, 1.25f, lastName = "#"),
    KeyData(Key.Four, "4", 4f, 1.25f, lastName = "$"),
    KeyData(Key.Five, "5", 5f, 1.25f, lastName = "%"),
    KeyData(Key.Six, "6", 6f, 1.25f, lastName = "^"),
    KeyData(Key.Seven, "7", 7f, 1.25f, lastName = "&"),
    KeyData(Key.Eight, "8", 8f, 1.25f, lastName = "*"),
    KeyData(Key.Nine, "9", 9f, 1.25f, lastName = "("),
    KeyData(Key.Zero, "0", 10f, 1.25f, lastName = ")"),
    KeyData(Key.Minus, "-", 11f, 1.25f, lastName = "_"),
    KeyData(Key.Equals, "=", 12f, 1.25f, lastName = "+"),
    KeyData(Key.Backspace, "Backspace", 13f, 1.25f, width = 2f),

    // ---- QWERTY 行 ----
    KeyData(Key.Tab, "Tab", 0f, 2.25f, width = 1.5f),
    KeyData(Key.Q, "Q", 1.5f, 2.25f),
    KeyData(Key.W, "W", 2.5f, 2.25f),
    KeyData(Key.E, "E", 3.5f, 2.25f),
    KeyData(Key.R, "R", 4.5f, 2.25f),
    KeyData(Key.T, "T", 5.5f, 2.25f),
    KeyData(Key.Y, "Y", 6.5f, 2.25f),
    KeyData(Key.U, "U", 7.5f, 2.25f),
    KeyData(Key.I, "I", 8.5f, 2.25f),
    KeyData(Key.O, "O", 9.5f, 2.25f),
    KeyData(Key.P, "P", 10.5f, 2.25f),
    KeyData(Key.LeftBracket, "[", 11.5f, 2.25f, lastName = "{"),
    KeyData(Key.RightBracket, "]", 12.5f, 2.25f, lastName = "}"),
    KeyData(Key.Backslash, "\\", 13.5f, 2.25f, width = 1.5f, lastName = "|"),

    // ---- HOME 行 ----
    KeyData(Key.CapsLock, "Caps Lock", 0f, 3.25f, width = 1.75f),
    KeyData(Key.A, "A", 1.75f, 3.25f),
    KeyData(Key.S, "S", 2.75f, 3.25f),
    KeyData(Key.D, "D", 3.75f, 3.25f),
    KeyData(Key.F, "F", 4.75f, 3.25f),
    KeyData(Key.G, "G", 5.75f, 3.25f),
    KeyData(Key.H, "H", 6.75f, 3.25f),
    KeyData(Key.J, "J", 7.75f, 3.25f),
    KeyData(Key.K, "K", 8.75f, 3.25f),
    KeyData(Key.L, "L", 9.75f, 3.25f),
    KeyData(Key.Semicolon, ";", 10.75f, 3.25f, lastName = ":"),
    KeyData(Key.Apostrophe, "'", 11.75f, 3.25f, lastName = "\""),
    KeyData(Key.Enter, "Enter", 12.75f, 3.25f, width = 2.25f),

    // ---- SHIFT 行 ----
    KeyData(Key.ShiftLeft, "Shift", 0f, 4.25f, width = 2.25f),
    KeyData(Key.Z, "Z", 2.25f, 4.25f),
    KeyData(Key.X, "X", 3.25f, 4.25f),
    KeyData(Key.C, "C", 4.25f, 4.25f),
    KeyData(Key.V, "V", 5.25f, 4.25f),
    KeyData(Key.B, "B", 6.25f, 4.25f),
    KeyData(Key.N, "N", 7.25f, 4.25f),
    KeyData(Key.M, "M", 8.25f, 4.25f),
    KeyData(Key.Comma, ",", 9.25f, 4.25f, lastName = "<"),
    KeyData(Key.Period, ".", 10.25f, 4.25f, lastName = ">"),
    KeyData(Key.Slash, "/", 11.25f, 4.25f, lastName = "?"),
    KeyData(Key.ShiftRight, "Shift", 12.25f, 4.25f, width = 2.75f),

    // ---- 底行 ----
    KeyData(Key.CtrlLeft, "Ctrl", 0f, 5.25f, width = 1.25f),
    KeyData(Key.Unknown, "", 1.25f, 5.25f, width = 1.25f),
    KeyData(Key.AltLeft, "Alt", 2.5f, 5.25f, width = 1.25f),
    KeyData(Key.Spacebar, "Space", 3.75f, 5.25f, width = 6.25f),
    KeyData(Key.AltRight, "Alt", 10f, 5.25f, width = 1.25f),
    KeyData(Key.Unknown, "", 11.25f, 5.25f, width = 1.25f),
    KeyData(4294967821, "Menu", null, 12.5f, 5.25f, width = 1.25f),
    KeyData(Key.CtrlRight, "Ctrl", 13.75f, 5.25f, width = 1.25f),
)

val controlArea = listOf(
    // ---- 第一行 (Y = 0) ----
    KeyData(Key.PrintScreen, "PrtSc", 0f, 0f),
    KeyData(Key.ScrollLock, "ScrLk", 1f, 0f),
    KeyData(4294967315, "Pause", null, 2f, 0f),

    // ---- 第二行 (Y = 1.25) ----
    KeyData(Key.Insert, "Ins", 0f, 1.25f),
    KeyData(Key.MoveHome, "Home", 1f, 1.25f),
    KeyData(Key.PageUp, "PgUp", 2f, 1.25f),

    // ---- 第三行 (Y = 2.25) ----
    KeyData(Key.Delete, "Del", 0f, 2.25f),
    KeyData(Key.MoveEnd, "End", 1f, 2.25f),
    KeyData(Key.PageDown, "PgDn", 2f, 2.25f),

    // ---- 方向键 (最底部) ----
    KeyData(Key.DirectionUp, "↑", 1f, 4.25f),
    KeyData(Key.DirectionLeft, "←", 0f, 5.25f),
    KeyData(Key.DirectionDown, "↓", 1f, 5.25f),
    KeyData(Key.DirectionRight, "→", 2f, 5.25f)
)

val numpadArea = listOf(
    KeyData(Key.NumLock, "Num", 0f, 1.25f),
    KeyData(Key.NumPadDivide, "/", 1f, 1.25f),
    KeyData(Key.NumPadMultiply, "*", 2f, 1.25f),
    KeyData(Key.NumPadSubtract, "-", 3f, 1.25f),

    KeyData(Key.NumPad7, "7", 0f, 2.25f),
    KeyData(Key.NumPad8, "8", 1f, 2.25f),
    KeyData(Key.NumPad9, "9", 2f, 2.25f),

    KeyData(Key.NumPad4, "4", 0f, 3.25f),
    KeyData(Key.NumPad5, "5", 1f, 3.25f),
    KeyData(Key.NumPad6, "6", 2f, 3.25f),

    KeyData(Key.NumPad1, "1", 0f, 4.25f),
    KeyData(Key.NumPad2, "2", 1f, 4.25f),
    KeyData(Key.NumPad3, "3", 2f, 4.25f),

    KeyData(Key.NumPad0, "0", 0f, 5.25f, width = 2f),
    KeyData(17179869294, ".", null, 2f, 5.25f),

    KeyData(Key.NumPadAdd, "+", 3f, 2.25f, height = 2f),
    KeyData(Key.NumPadEnter, "Enter", 3f, 4.25f, height = 2f)
)

val keyCodeToShortName = mapOf(
    // ===== 主键盘区 =====
    // F 区
    Key.Escape.keyCode to "ESC",
    Key.F1.keyCode to "F1", Key.F2.keyCode to "F2", Key.F3.keyCode to "F3", Key.F4.keyCode to "F4",
    Key.F5.keyCode to "F5", Key.F6.keyCode to "F6", Key.F7.keyCode to "F7", Key.F8.keyCode to "F8",
    Key.F9.keyCode to "F9", Key.F10.keyCode to "F10", Key.F11.keyCode to "F11", Key.F12.keyCode to "F12",

    // 数字行
    Key.Grave.keyCode to "`",
    Key.One.keyCode to "1", Key.Two.keyCode to "2", Key.Three.keyCode to "3", Key.Four.keyCode to "4",
    Key.Five.keyCode to "5", Key.Six.keyCode to "6", Key.Seven.keyCode to "7", Key.Eight.keyCode to "8",
    Key.Nine.keyCode to "9", Key.Zero.keyCode to "0",
    Key.Minus.keyCode to "-",
    Key.Equals.keyCode to "=",
    Key.Backspace.keyCode to "BS",

    // QWERTY 行
    Key.Tab.keyCode to "Tab",
    Key.Q.keyCode to "Q", Key.W.keyCode to "W", Key.E.keyCode to "E", Key.R.keyCode to "R",
    Key.T.keyCode to "T", Key.Y.keyCode to "Y", Key.U.keyCode to "U", Key.I.keyCode to "I",
    Key.O.keyCode to "O", Key.P.keyCode to "P",
    Key.LeftBracket.keyCode to "[",
    Key.RightBracket.keyCode to "]",
    Key.Backslash.keyCode to "\\",      // 反斜杠

    // HOME 行
    Key.CapsLock.keyCode to "Cap",
    Key.A.keyCode to "A", Key.S.keyCode to "S", Key.D.keyCode to "D", Key.F.keyCode to "F",
    Key.G.keyCode to "G", Key.H.keyCode to "H", Key.J.keyCode to "J", Key.K.keyCode to "K",
    Key.L.keyCode to "L",
    Key.Semicolon.keyCode to ";",
    Key.Apostrophe.keyCode to "'",
    Key.Enter.keyCode to "Ent",

    // SHIFT 行
    Key.ShiftLeft.keyCode to "SL",
    Key.Z.keyCode to "Z", Key.X.keyCode to "X", Key.C.keyCode to "C", Key.V.keyCode to "V",
    Key.B.keyCode to "B", Key.N.keyCode to "N", Key.M.keyCode to "M",
    Key.Comma.keyCode to ",",
    Key.Period.keyCode to ".",
    Key.Slash.keyCode to "/",
    Key.ShiftRight.keyCode to "SR",

    // 底行
    Key.CtrlLeft.keyCode to "CL",
    Key.AltLeft.keyCode to "AL",
    Key.Spacebar.keyCode to "Spc",
    Key.AltRight.keyCode to "AR",
    Key.CtrlRight.keyCode to "CR",
    4294967821L to "Men",       // Menu 键

    // ===== 控制区 =====
    Key.PrintScreen.keyCode to "Prt",
    Key.ScrollLock.keyCode to "Scr",
    4294967315L to "Pau",       // Pause
    Key.Insert.keyCode to "Ins",
    Key.MoveHome.keyCode to "Hom",
    Key.PageUp.keyCode to "PUp",
    Key.Delete.keyCode to "Del",
    Key.MoveEnd.keyCode to "End",
    Key.PageDown.keyCode to "PDn",

    // 方向键
    Key.DirectionUp.keyCode to "Up",
    Key.DirectionLeft.keyCode to "Lt",
    Key.DirectionDown.keyCode to "Dn",
    Key.DirectionRight.keyCode to "Rt",

    // ===== 数字键盘区 =====
    Key.NumLock.keyCode to "Num",
    Key.NumPadDivide.keyCode to "/",
    Key.NumPadMultiply.keyCode to "*",
    Key.NumPadSubtract.keyCode to "-",
    Key.NumPadAdd.keyCode to "+",
    Key.NumPadEnter.keyCode to "NEn",
    Key.NumPad7.keyCode to "7", Key.NumPad8.keyCode to "8", Key.NumPad9.keyCode to "9",
    Key.NumPad4.keyCode to "4", Key.NumPad5.keyCode to "5", Key.NumPad6.keyCode to "6",
    Key.NumPad1.keyCode to "1", Key.NumPad2.keyCode to "2", Key.NumPad3.keyCode to "3",
    Key.NumPad0.keyCode to "0",
    17179869294L to "."          // 小数点
)