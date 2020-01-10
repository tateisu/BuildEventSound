package settingsUi

import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.Spacer
import java.awt.Dimension
import java.awt.Insets
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class Form1 {

    companion object {

        private fun gridConstraints(
            row: Int,
            column: Int,
            rowSpan: Int = 1,
            colSpan: Int = 1,
            anchor: Int,
            fill: Int,
            HSizePolicy: Int,
            VSizePolicy: Int,
            minimumSize: Dimension? = null,
            preferredSize: Dimension? = null,
            maximumSize: Dimension? = null,
            indent: Int = 0,
            useParentLayout: Boolean = false
        ) = GridConstraints(
            row,
            column,
            rowSpan,
            colSpan,
            anchor,
            fill,
            HSizePolicy,
            VSizePolicy,
            minimumSize,
            preferredSize,
            maximumSize,
            indent,
            useParentLayout
        )
    }

    val textFieldConfigPath = JTextField()

    val rootPanel: JPanel = JPanel().apply {

        layout = GridLayoutManager(
            2,
            2,
            Insets(0, 0, 0, 0),
            -1,
            -1
        ).apply {
            isSameSizeHorizontally = false
            isSameSizeVertically = false
        }

        isRequestFocusEnabled = true

        add(
            JLabel().apply {
                text = "Config file path"
                labelFor = textFieldConfigPath
            },
            gridConstraints(
                row = 0,
                column = 0,
                anchor = GridConstraints.ANCHOR_WEST,
                fill = GridConstraints.FILL_NONE,
                HSizePolicy = GridConstraints.SIZEPOLICY_FIXED,
                VSizePolicy = GridConstraints.SIZEPOLICY_FIXED
            )
        )

        add(
            Spacer(),
            gridConstraints(
                row = 1,
                column = 0,
                anchor = GridConstraints.ANCHOR_WEST,
                fill = GridConstraints.FILL_VERTICAL,
                HSizePolicy = GridConstraints.SIZEPOLICY_CAN_SHRINK,
                VSizePolicy = GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_WANT_GROW
            )
        )

        add(
            textFieldConfigPath,
            gridConstraints(
                row = 0,
                column = 1,
                anchor = GridConstraints.ANCHOR_WEST,
                fill = GridConstraints.FILL_HORIZONTAL,
                HSizePolicy = GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_WANT_GROW,
                VSizePolicy = GridConstraints.SIZEPOLICY_FIXED
            )
        )
    }
}
