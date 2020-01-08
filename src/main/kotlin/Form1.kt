import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.Spacer
import java.awt.Insets
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class Form1 {

    val textFieldConfigPath = JTextField()
    val rootPanel: JPanel = JPanel()

    init {

        rootPanel.apply {

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

            val label1 =  JLabel().apply {
                text = "Config file path"
            }
            add(
                label1,
                GridConstraints(
                    /*row =*/ 0,
                    /*column = */0,
                    /*rowSpan =*/ 1,
                    /*colSpan =*/ 1,
                    /*anchor = */GridConstraints.ANCHOR_WEST,
                    /*fill = */GridConstraints.FILL_NONE,
                    /*HSizePolicy =*/ 0,
                    /*VSizePolicy =*/ 0,
                    /*minimumSize =*/ null,
                    /* preferredSize = */null,
                    /* maximumSize = */null,
                    /*indent = */0,
                    /* useParentLayout=*/  false
                )
            )

            add(
                Spacer(),
                GridConstraints(
                    /*row =*/ 1,
                    /*column = */0,
                    /*rowSpan =*/ 1,
                    /*colSpan =*/ 1,
                    /*anchor = */0,
                    /*fill = */2,
                    /*HSizePolicy =*/ 1,
                    /*VSizePolicy =*/ 6,
                    /*minimumSize =*/ null,
                    /* preferredSize = */null,
                    /* maximumSize = */null,
                    /*indent = */0,
                    /* useParentLayout=*/  false
                )
            )

            add(
                textFieldConfigPath,
                GridConstraints(
                    /*row =*/ 0,
                    /*column = */1,
                    /*rowSpan =*/ 1,
                    /*colSpan =*/ 1,
                    /*anchor = */8,
                    /*fill = */1,
                    /*HSizePolicy =*/ 6,
                    /*VSizePolicy =*/ 0,
                    /*minimumSize =*/ null,
                    /* preferredSize = */ null,
                    /* maximumSize = */null,
                    /*indent = */0,
                    /* useParentLayout=*/  false
                )
            )

            label1.labelFor = textFieldConfigPath
        }
    }
}
