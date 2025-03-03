package com.blogspot.kotlinstudy.lognote

import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.ListSelectionListener


abstract class CustomListManager (mainUI: MainUI, logPanel: LogPanel){
    companion object {
        const val CMD_NEW = 1
        const val CMD_COPY = 2
        const val CMD_EDIT = 3
    }

    protected val mMainUI = mainUI
    private val mLogPanel = logPanel
    var mDialogTitle = "Custom List"
    private var mFirstElement: CustomElement? = null
    private var mCustomDialog: CustomDialog? = null

    abstract fun loadList(): ArrayList<CustomElement>
    abstract fun saveList(list: ArrayList<CustomElement>)
    abstract fun getFirstElement(): CustomElement
    abstract fun getListSelectionListener(): ListSelectionListener
    abstract fun getListMouseListener(): MouseListener
    abstract fun getListKeyListener(): KeyListener

    fun showDialog() {
        if (mCustomDialog == null) {
            mCustomDialog = CustomDialog(mMainUI)
        }
        mCustomDialog?.initDialog()
        mCustomDialog?.setLocationRelativeTo(mMainUI)
        mCustomDialog?.isVisible = true
    }

    class CustomElement(title: String, value: String, tableBar: Boolean) {
        var mTitle = title
        var mValue = value
        var mTableBar = tableBar
    }

    internal inner class CustomDialog (parent: MainUI) : JDialog(parent, mDialogTitle, true), ActionListener {
        private var mScrollPane: JScrollPane
        var mList = JList<CustomElement>()
        private var mFirstBtn: ColorButton
        private var mPrevBtn: ColorButton
        private var mNextBtn: ColorButton
        private var mLastBtn: ColorButton
        private var mNewBtn: ColorButton
        private var mCopyBtn: ColorButton
        private var mEditBtn: ColorButton
        private var mDeleteBtn: ColorButton
        private var mSaveBtn: ColorButton
        private var mCloseBtn: ColorButton
        private var mModel = DefaultListModel<CustomElement>()

        init {
            mList = JList<CustomElement>()
            mList.model = mModel

            val selectionListener = getListSelectionListener()
            mList.addListSelectionListener(selectionListener)

            val mouseListener = getListMouseListener()
            mList.addMouseListener(mouseListener)

            val keyListener = getListKeyListener()
            mList.addKeyListener(keyListener)

            mList.cellRenderer = CustomCellRenderer()

            val componentListener: ComponentListener = object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) {
                    mList.fixedCellHeight = 10
                    mList.fixedCellHeight = -1
                }
            }

            mList.addComponentListener(componentListener)
            mScrollPane = JScrollPane(mList)
            mScrollPane.preferredSize = Dimension(800, 500)

            mFirstBtn = ColorButton("↑")
            mFirstBtn.addActionListener(this)
            mPrevBtn = ColorButton("∧")
            mPrevBtn.addActionListener(this)
            mNextBtn = ColorButton("∨")
            mNextBtn.addActionListener(this)
            mLastBtn = ColorButton("↓")
            mLastBtn.addActionListener(this)

            mNewBtn = ColorButton(Strings.NEW)
            mNewBtn.addActionListener(this)
            mCopyBtn = ColorButton(Strings.COPY)
            mCopyBtn.addActionListener(this)
            mEditBtn = ColorButton(Strings.EDIT)
            mEditBtn.addActionListener(this)
            mDeleteBtn = ColorButton(Strings.DELETE)
            mDeleteBtn.addActionListener(this)
            mSaveBtn = ColorButton(Strings.SAVE)
            mSaveBtn.addActionListener(this)
            mCloseBtn = ColorButton(Strings.CLOSE)
            mCloseBtn.addActionListener(this)
            val bottomPanel = JPanel()
            bottomPanel.add(mFirstBtn)
            bottomPanel.add(mPrevBtn)
            bottomPanel.add(mNextBtn)
            bottomPanel.add(mLastBtn)
            addVSeparator(bottomPanel)
            bottomPanel.add(mNewBtn)
            bottomPanel.add(mCopyBtn)
            bottomPanel.add(mEditBtn)
            bottomPanel.add(mDeleteBtn)
            addVSeparator(bottomPanel)
            bottomPanel.add(mSaveBtn)
            bottomPanel.add(mCloseBtn)

            contentPane.layout = BorderLayout()
            contentPane.add(mScrollPane, BorderLayout.CENTER)
            contentPane.add(bottomPanel, BorderLayout.SOUTH)

            pack()

            Utils.installKeyStrokeEscClosing(this)
        }

        private fun addVSeparator(panel:JPanel) {
            val separator1 = JSeparator(SwingConstants.VERTICAL)
            separator1.preferredSize = Dimension(separator1.preferredSize.width, 20)
            if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
                separator1.foreground = Color.GRAY
                separator1.background = Color.GRAY
            }
            else {
                separator1.foreground = Color.DARK_GRAY
                separator1.background = Color.DARK_GRAY
            }
            panel.add(separator1)
        }

        fun initDialog() {
            val customListArray = loadList()
            mModel.clear()
            mFirstElement = getFirstElement()
            if (mFirstElement != null) {
                mModel.addElement(mFirstElement)
            }

            for (item in customListArray) {
                mModel.addElement(item)
            }
        }
        inner class CustomCellRenderer : ListCellRenderer<Any?> {
            private val cellPanel: JPanel
            private val titlePanel: JPanel
            private val titleLabel: JLabel
            private val valueTA: JTextArea

            override fun getListCellRendererComponent(
                list: JList<*>,
                value: Any?, index: Int, isSelected: Boolean,
                hasFocus: Boolean
            ): Component {
                val element = value as CustomElement
                titleLabel.text = element.mTitle
                if (mFirstElement != null && mFirstElement!!.mTitle == element.mTitle) {
                    if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
                        titleLabel.foreground = Color(0xC05050)
                    }
                    else {
                        titleLabel.foreground = Color(0x900000)
                    }
                } else if (element.mTableBar) {
                    titleLabel.text += " - TableBar"
                    if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
                        titleLabel.foreground = Color(0x50C050)
                    }
                    else {
                        titleLabel.foreground = Color(0x009000)
                    }
                }
                else {
                    if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
                        titleLabel.foreground = Color(0x7070E0)
                    }
                    else {
                        titleLabel.foreground = Color(0x000090)
                    }
                }
                valueTA.text = element.mValue

                valueTA.updateUI()

                if (isSelected) {
                    if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
                        titlePanel.background = Color(0x56595B)
                        valueTA.background = Color(0x56595B)
                    }
                    else {
                        titlePanel.background = Color.LIGHT_GRAY
                        valueTA.background = Color.LIGHT_GRAY
                    }
                }
                else {
                    if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
                        titlePanel.background = Color(0x46494B)
                        valueTA.background = Color(0x46494B)
                    }
                    else {
                        titlePanel.background = Color.WHITE
                        valueTA.background = Color.WHITE
                    }
                }
                return cellPanel
            }

            init {
                cellPanel = JPanel(BorderLayout())

                titlePanel = JPanel(BorderLayout())
                titleLabel = JLabel("")
                titleLabel.foreground = Color(0x000090)
                titleLabel.font = titleLabel.font.deriveFont(titleLabel.font.style or Font.BOLD)
                titlePanel.add(titleLabel, BorderLayout.NORTH)
                cellPanel.add(titlePanel, BorderLayout.NORTH)

                valueTA = JTextArea()
                valueTA.lineWrap = true
                valueTA.wrapStyleWord = true
                cellPanel.add(valueTA, BorderLayout.CENTER)
            }
        }

        override fun actionPerformed(e: ActionEvent?) {
            if (e?.source == mFirstBtn) {
                val startIdx = if (mFirstElement == null) 0 else 1
                mList.valueIsAdjusting = true
                val selectedIdx = mList.selectedIndex
                if (mModel.size >= (3 - startIdx)) {
                    val selection = mList.selectedValue
                    if (mFirstElement == null || mFirstElement!!.mTitle != selection.mTitle) {
                        mModel.remove(selectedIdx)
                        mModel.add(startIdx, selection)
                        mList.selectedIndex = startIdx
                    }
                }
                mList.valueIsAdjusting = false
            } else if (e?.source == mPrevBtn) {
                val startIdx = if (mFirstElement == null) 0 else 1
                mList.valueIsAdjusting = true
                val selectedIdx = mList.selectedIndex
                if (mModel.size >= (3 - startIdx) && selectedIdx > startIdx) {
                    val selection = mList.selectedValue
                    if (mFirstElement == null || mFirstElement!!.mTitle != selection.mTitle) {
                        mModel.remove(selectedIdx)
                        mModel.add(selectedIdx - 1, selection)
                        mList.selectedIndex = selectedIdx - 1
                    }
                }
                mList.valueIsAdjusting = false
            } else if (e?.source == mNextBtn) {
                val startIdx = if (mFirstElement == null) 0 else 1
                mList.valueIsAdjusting = true
                val selectedIdx = mList.selectedIndex
                if (mModel.size >= (3 - startIdx) && selectedIdx >= startIdx && selectedIdx < (mModel.size() - 1)) {
                    val selection = mList.selectedValue
                    if (mFirstElement == null || mFirstElement!!.mTitle != selection.mTitle) {
                        mModel.remove(selectedIdx)
                        mModel.add(selectedIdx + 1, selection)
                        mList.selectedIndex = selectedIdx + 1
                    }
                }
                mList.valueIsAdjusting = false
            } else if (e?.source == mLastBtn) {
                val startIdx = if (mFirstElement == null) 0 else 1
                mList.valueIsAdjusting = true
                val selectedIdx = mList.selectedIndex
                if (mModel.size >= (3 - startIdx)) {
                    val selection = mList.selectedValue
                    if (mFirstElement == null || mFirstElement!!.mTitle != selection.mTitle) {
                        mModel.remove(selectedIdx)
                        mModel.add(mModel.size(), selection)
                        mList.selectedIndex = mModel.size() - 1
                    }
                }
                mList.valueIsAdjusting = false
            } else if (e?.source == mNewBtn) {
                val editDialog = EditDialog(this, CMD_NEW, "", "", false)
                editDialog.setLocationRelativeTo(this)
                editDialog.isVisible = true
            } else if (e?.source == mCopyBtn) {
                if (mList.selectedIndex >= 0) {
                    val selection = mList.selectedValue
                    val editDialog = EditDialog(this, CMD_COPY, selection.mTitle, selection.mValue, selection.mTableBar)
                    editDialog.setLocationRelativeTo(this)
                    editDialog.isVisible = true
                }
            } else if (e?.source == mEditBtn) {
                if (mList.selectedIndex >= 0) {
                    val selection = mList.selectedValue
                    val cmd = if (mFirstElement == null || mFirstElement!!.mTitle != selection.mTitle) {
                        CMD_EDIT
                    } else {
                        CMD_COPY
                    }
                    val editDialog = EditDialog(this, cmd, selection.mTitle, selection.mValue, selection.mTableBar)
                    editDialog.setLocationRelativeTo(this)
                    editDialog.isVisible = true
                }
            } else if (e?.source == mDeleteBtn) {
                if (mList.selectedIndex >= 0) {
                    mList.valueIsAdjusting = true
                    val selectedIdx = mList.selectedIndex
                    mModel.remove(mList.selectedIndex)
                    if (selectedIdx > 0) {
                        mList.selectedIndex = selectedIdx - 1
                    }
                    mList.valueIsAdjusting = false
                }
            } else if (e?.source == mSaveBtn) {
                val customListArray = ArrayList<CustomElement>()
                for (item in mModel.elements()) {
                    if (mFirstElement == null || mFirstElement!!.mTitle != item.mTitle) {
                        customListArray.add(item)
                    }
                }

                saveList(customListArray)

                mLogPanel.updateTableBar(customListArray)
            } else if (e?.source == mCloseBtn) {
                dispose()
            }
        }

        private fun updateElement(cmd: Int, prevTitle: String, element: CustomElement) {
            if (cmd == CMD_EDIT) {
                for (item in mModel.elements()) {
                    if (item.mTitle == title) {
                        item.mValue = element.mValue
                        return
                    }
                }
                mList.valueIsAdjusting = true
                val selectedIdx = mList.selectedIndex
                mModel.remove(selectedIdx)
                mModel.add(selectedIdx, element)
                mList.selectedIndex = selectedIdx
                mList.valueIsAdjusting = false
            }
            else {
                mModel.addElement(element)
            }
        }

        internal inner class EditDialog(parent: CustomDialog, cmd: Int, title: String, value: String, tableBar: Boolean) :JDialog(parent, "Edit", true), ActionListener {
            private var mOkBtn: ColorButton
            private var mCancelBtn: ColorButton

            private var mTitleLabel: JLabel
            private var mValueLabel: JLabel
            private var mTableBarLabel: JLabel

            private var mTitleTF: JTextField
            private var mValueTF: JTextField
            private var mTableBarCheck: JCheckBox

            private var mTitleStatusLabel: JLabel
            private var mValueStatusLabel: JLabel

            private var mParent = parent
            private var mPrevTitle = title
            private var mCmd = cmd

            init {
                mOkBtn = ColorButton(Strings.OK)
                mOkBtn.addActionListener(this)
                mCancelBtn = ColorButton(Strings.CANCEL)
                mCancelBtn.addActionListener(this)

                mTitleLabel = JLabel("Title")
                mTitleLabel.preferredSize = Dimension(50, 30)
                mValueLabel = JLabel("Value")
                mValueLabel.preferredSize = Dimension(50, 30)
                mTableBarLabel = JLabel("Add TableBar")

                mTitleTF = JTextField(title)
                mTitleTF.document.addDocumentListener(TitleDocumentHandler())
                mTitleTF.preferredSize = Dimension(488, 30)
                mValueTF = JTextField(value)
                mValueTF.document.addDocumentListener(ValueDocumentHandler())
                mValueTF.preferredSize = Dimension(488, 30)

                mTableBarCheck = JCheckBox()
                mTableBarCheck.isSelected = tableBar

                mTitleStatusLabel = JLabel("Good")
                mValueStatusLabel = JLabel("Good")

                val titleStatusPanel = JPanel(FlowLayout(FlowLayout.CENTER))
                titleStatusPanel.border = BorderFactory.createLineBorder(Color.LIGHT_GRAY)
                titleStatusPanel.add(mTitleStatusLabel)
                titleStatusPanel.preferredSize = Dimension(200, 30)

                val valueStatusPanel = JPanel(FlowLayout(FlowLayout.CENTER))
                valueStatusPanel.border = BorderFactory.createLineBorder(Color.LIGHT_GRAY)
                valueStatusPanel.add(mValueStatusLabel)
                valueStatusPanel.preferredSize = Dimension(200, 30)


                val panel1 = JPanel(GridLayout(2, 1, 0, 2))
                panel1.add(mTitleLabel)
                panel1.add(mValueLabel)

                val panel2 = JPanel(GridLayout(2, 1, 0, 2))
                panel2.add(mTitleTF)
                panel2.add(mValueTF)

                val panel3 = JPanel(GridLayout(2, 1, 0, 2))
                panel3.add(titleStatusPanel)
                panel3.add(valueStatusPanel)

                val titleValuePanel = JPanel()
                titleValuePanel.add(panel1)
                titleValuePanel.add(panel2)
                titleValuePanel.add(panel3)

                val tableBarPanel = JPanel()
                tableBarPanel.add(mTableBarLabel)
                tableBarPanel.add(mTableBarCheck)

                val confirmPanel = JPanel()
                confirmPanel.preferredSize = Dimension(400, 40)
                confirmPanel.add(mOkBtn)
                confirmPanel.add(mCancelBtn)

                val panel = JPanel()
                panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
                panel.add(titleValuePanel)
                panel.add(tableBarPanel)
                panel.add(confirmPanel)

                contentPane.add(panel)
                pack()

                var isValid = true
                if (mTitleTF.text.trim().isEmpty()) {
                    mTitleStatusLabel.text = "Empty"
                    isValid = false
                }
                else if (mFirstElement != null && mFirstElement!!.mTitle == mTitleTF.text.trim()) {
                    mTitleStatusLabel.text = "Not allow : ${mFirstElement!!.mTitle}"
                    isValid = false
                }
                else if (cmd == CMD_COPY) {
                    mTitleStatusLabel.text = "Copy : duplicated"
                    isValid = false
                }

                if (isValid) {
                    if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
                        mTitleStatusLabel.foreground = Color(0x7070C0)
                    } else {
                        mTitleStatusLabel.foreground = Color.BLUE
                    }
                }
                else {
                    if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
                        mTitleStatusLabel.foreground = Color(0xC07070)
                    }
                    else {
                        mTitleStatusLabel.foreground = Color.RED
                    }
                }

                if (mValueTF.text.trim().isEmpty()) {
                    if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
                        mValueStatusLabel.foreground = Color(0xC07070)
                    }
                    else {
                        mValueStatusLabel.foreground = Color.RED
                    }
                    mValueStatusLabel.text = "Empty"
                    isValid = false
                }
                else {
                    if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
                        mValueStatusLabel.foreground = Color(0x7070C0)
                    } else {
                        mValueStatusLabel.foreground = Color.BLUE
                    }
                }

                mOkBtn.isEnabled = isValid

                Utils.installKeyStrokeEscClosing(this)
            }

            override fun actionPerformed(e: ActionEvent?) {
                if (e?.source == mOkBtn) {
                    mParent.updateElement(mCmd, mPrevTitle, CustomElement(mTitleTF.text, mValueTF.text, mTableBarCheck.isSelected))
                    dispose()
                } else if (e?.source == mCancelBtn) {
                    dispose()
                }
            }

            internal inner class TitleDocumentHandler: DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) {
                    checkText(e)
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    checkText(e)
                }

                override fun changedUpdate(e: DocumentEvent?) {
                    checkText(e)
                }
                private fun checkText(e: DocumentEvent?) {
                    var isValid = true
                    val title = mTitleTF.text.trim()
                    if (title.isEmpty()) {
                        mTitleStatusLabel.text = "Empty"
                        isValid = false
                    }
                    else if (mFirstElement != null && mFirstElement!!.mTitle == title) {
                        mTitleStatusLabel.text = "Not allow : ${mFirstElement!!.mTitle}"
                        isValid = false
                    }
                    else {
                        for (item in mModel.elements()) {
                            if (item.mTitle == title) {
                                if (mCmd != CMD_EDIT || (mCmd == CMD_EDIT && mPrevTitle != title)) {
                                    mTitleStatusLabel.text = "Duplicated"
                                    isValid = false
                                }
                                break
                            }
                        }
                    }

                    if (isValid) {
                        if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
                            mTitleStatusLabel.foreground = Color(0x7070C0)
                        }
                        else {
                            mTitleStatusLabel.foreground = Color.BLUE
                        }
                        mTitleStatusLabel.text = "Good"
                    }
                    else {
                        if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
                            mTitleStatusLabel.foreground = Color(0xC07070)
                        }
                        else {
                            mTitleStatusLabel.foreground = Color.RED
                        }
                    }

                    mOkBtn.isEnabled = mTitleStatusLabel.text == "Good" && mValueStatusLabel.text == "Good"
                }
            }

            internal inner class ValueDocumentHandler: DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) {
                    checkText(e)
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    checkText(e)
                }

                override fun changedUpdate(e: DocumentEvent?) {
                    checkText(e)
                }
                private fun checkText(e: DocumentEvent?) {
                    var isValid = true

                    val value = mValueTF.text.trim()
                    if (value.isEmpty()) {
                        mValueStatusLabel.text = "Empty"
                        isValid = false
                    }

                    if (isValid) {
                        if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
                            mValueStatusLabel.foreground = Color(0x7070C0)
                        }
                        else {
                            mValueStatusLabel.foreground = Color.BLUE
                        }
                        mValueStatusLabel.text = "Good"
                    }
                    else {
                        if (ConfigManager.LaF == MainUI.FLAT_DARK_LAF) {
                            mValueStatusLabel.foreground = Color(0xC07070)
                        }
                        else {
                            mValueStatusLabel.foreground = Color.RED
                        }
                    }

                    mOkBtn.isEnabled = mTitleStatusLabel.text == "Good" && mValueStatusLabel.text == "Good"
                }
            }
        }
    }
}
