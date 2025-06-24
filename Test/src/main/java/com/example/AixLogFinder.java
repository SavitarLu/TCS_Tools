package com.example;
import java.text.SimpleDateFormat;
import DB2.*;
import com.jcraft.jsch.*;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.stream.Collectors;

import javax.swing.border.TitledBorder;

import static DB2.DB2Util.*;

public class AixLogFinder extends JFrame {
    private static final String TCS_CONFIG_FILE = "sessions_TCS.dat";
    private static final String MES_CONFIG_FILE = "sessions_MES.dat";
    private static String CONFIG_FILE = " ";
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final Color CONNECTED_COLOR = new Color(0, 255, 0);
    private static final Color DISCONNECTED_COLOR = new Color(255, 0, 0);
    private static final Color DEFAULT_COLOR = new Color(255, 255, 255);

    private JList<SessionConfig> sessionList;
    private DefaultListModel<SessionConfig> sessionListModel;
    private JTextField hostField, portField, userField, passwordField;
    private JPasswordField passwordFieldActual;
    private JButton addSessionBtn, removeSessionBtn, connectBtn, searchBtn, openBtn,chooseDownloadPathBtn, varBtn;
    private JList<String> resultList;
    private DefaultListModel<String> resultListModel;
    private JScrollPane resultScrollPane;
    private JSplitPane mainSplitPane, leftSplitPane;
    private List<String> searchResults = new ArrayList<>();
    private Session currentSession;
    private List<SessionConfig> sessionConfigs = new ArrayList<>();
    private JComboBox<String> editorComboBox;
    private JLabel sessionStatusDot; // 连接状态点
    private JLabel eqpStatusDot; // 连接状态点
    private JLabel currentUserLabel,downloadPathLabel; // 当前用户标签
    private JTextField searchField; // 输入框
    private JList<String> suggestionList; // 备选列表
    private DefaultListModel<String> suggestionModel; // 备选列表数据模型
    private JPopupMenu suggestionPopup; // 备选菜单
    private List<String> localDataList = new ArrayList<>(); // 本地数据源
    private Timer filterTimer = new Timer(); // 添加成员变量
    private JSplitPane sessionPortSplitPane;
    private static final String DOWNLOAD_CONFIG_FILE = "download_path.dat"; // 下载路径配置文件
    private String downloadPath = TEMP_DIR; // 默认路径为系统临时目录

    private JLabel portLabel; // 端口显示标签
    private Map<String, String> tcsIdPortMap = new HashMap<>(); // 机台ID与端口映射

    private JTextField tcsIdField; // 机台ID输入框
    private JTextField eqpStateField; // 机台ID输入框
    private JTextField timesp; // 机台ID输入框
    private JList<String> tcsSuggestionList; // 机台ID备选列表
    private DefaultListModel<String> tcsSuggestionModel; // 备选列表数据模型
    private JPopupMenu tcsSuggestionPopup; // 备选菜单
    private Timer tcsFilterTimer = new Timer(); // 过滤定时器

    private JButton connectTcsBtn; // 连接TCS按钮
    private JButton VisualBtn; // 连接TCS按钮

    private JPanel portInfoPanel; // 新增：用于显示端口信息的面板
    private boolean showPortInfo = false; // 控制端口信息面板是否显示的标志
    private boolean isMES; // 新增MES模式标志
    // 在类成员中添加按钮声明
    private JButton modeSwitchBtn; // 模式切换按钮
    private JPanel sessionPanel,searchPanel;
    private JSpinner dateSpinner; // 日期选择器
    private JFormattedTextField dateField; // 日期输入字段
    private JLabel dateLabel; // 日期标签
    private JLabel timeLabel; // 日期标签
    public AixLogFinder(boolean isMES) {
        this.isMES = isMES; // 保存模式状态
        initComponents();
        loadLocalData(isMES ? "txList.txt" : "tcsxList.txt"); // 根据模式加载不同数据源
        loadSessions();
        loadDownloadPath();
        setupLayout();
        setupListeners();
        setTitle(isMES ? "MES实用小工具" : "TCS实用小工具");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        if (isMES) {
            searchField.requestFocusInWindow(); // MES模式聚焦搜索框
        } else {
            tcsIdField.requestFocusInWindow(); // TCS模式聚焦机台ID
        }
    }

    private void initComponents() {
        sessionListModel = new DefaultListModel<>();
        sessionList = new JList<>(sessionListModel);
        sessionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sessionPortSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sessionPortSplitPane.setResizeWeight(0.7); // 70%空间给会话管理
        sessionPortSplitPane.setDividerSize(5); // 分割条大小
        sessionPortSplitPane.setOneTouchExpandable(true); // 添加快速展开/折叠按钮

        sessionPortSplitPane.setTopComponent(sessionPanel);
        sessionPortSplitPane.setBottomComponent(portInfoPanel);
        sessionPortSplitPane.setDividerLocation(400); // 初始分割位置

        // 修改左侧整体布局
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(sessionPortSplitPane, BorderLayout.CENTER);

        portInfoPanel = new JPanel(new GridBagLayout());
        portInfoPanel.setBorder(BorderFactory.createTitledBorder("端口信息"));
        portInfoPanel.setVisible(false); // 初始时隐藏

        hostField = new JTextField("10.196.60.107");
        portField = new JTextField("22");
        userField = new JTextField("ls2wtcs");
        passwordFieldActual = new JPasswordField("ls2wtcs");

        addSessionBtn = new JButton("添加会话");
        removeSessionBtn = new JButton("删除会话");
        connectBtn = new JButton("连接");
        searchBtn = new JButton("搜索");
        varBtn = new JButton("分类");
        openBtn = new JButton("打开文件");

        resultListModel = new DefaultListModel<>();
        resultList = new JList<>(resultListModel);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultScrollPane = new JScrollPane(resultList);

        String[] defaultEditors = {"系统默认", "记事本", "VS Code", "Sublime Text"};
        editorComboBox = new JComboBox<>(defaultEditors);

        // 连接状态组件
        sessionStatusDot = new JLabel();
        sessionStatusDot.setPreferredSize(new Dimension(12, 12));
        sessionStatusDot.setBorder(BorderFactory.createEtchedBorder());
        currentUserLabel = new JLabel("未连接"); // 显示当前用户或状态
        updateStatus(false, null); // 初始化状态

        searchField = new JTextField();
        suggestionModel = new DefaultListModel<>();
        suggestionList = new JList<>(suggestionModel);
        suggestionPopup = new JPopupMenu();
        suggestionPopup.setFocusable(false); // 菜单不获取焦点
        suggestionPopup.add(new JScrollPane(suggestionList));

        // 初始化机台ID/TX ID输入框组件
        tcsSuggestionModel = new DefaultListModel<>();
        tcsSuggestionList = new JList<>(tcsSuggestionModel);
        tcsSuggestionPopup = new JPopupMenu();
        tcsSuggestionPopup.setFocusable(false);
        tcsSuggestionPopup.add(new JScrollPane(tcsSuggestionList));

        tcsIdField = new JTextField();

        connectTcsBtn = new JButton("连接TCS");

        eqpStatusDot = new JLabel();
        eqpStateField = new JTextField();
        VisualBtn = new JButton("可视化连接");

        // 初始化端口标签
        portLabel = new JLabel();
        portLabel.setForeground(Color.GRAY);
        portLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0)); // 左边距5像素

        // 新增模式切换按钮
        modeSwitchBtn = new JButton("切换模式");
        modeSwitchBtn.setToolTipText("在TCS和MES模式之间切换");

        // 初始化日期选择器
        dateLabel = new JLabel("日期:");
        timeLabel = new JLabel("时间:");
        timesp = new JTextField();
        // 创建日期格式
        SpinnerDateModel dateModel = new SpinnerDateModel(
                new Date(),                   // 初始日期为当前日期
                null,                         // 最小日期(无)
                null,                         // 最大日期(无)
                Calendar.DAY_OF_MONTH         // 使用Calendar类的DAY_OF_MONTH常量
        );

        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyyMMdd");
        dateSpinner.setEditor(dateEditor);

        // 通过类型转换获取文本字段
        JComponent editorComponent = dateSpinner.getEditor();
        if (editorComponent instanceof JSpinner.DefaultEditor) {
            dateField = ((JSpinner.DefaultEditor) editorComponent).getTextField();
            dateField.setColumns(8); // 设置输入框宽度
        } else {
            // 备用方案：直接创建一个格式化文本字段
            dateField = new JFormattedTextField(new SimpleDateFormat("yyyyMMdd"));
            dateField.setColumns(8);
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyyMMdd"));
        }
    }

    private void loadLocalData(String dataFile) {
        try {
            Path dataPath = Paths.get(dataFile);
            List<String> lines = Files.lines(dataPath)
                    .filter(line -> !line.trim().isEmpty() && !line.startsWith("#"))
                    .collect(Collectors.toList());

            // 检测文件格式（MES模式可能只有一列）
            boolean isMultiColumnFormat = false;
            if (!lines.isEmpty()) {
                String firstLine = lines.get(0);
                isMultiColumnFormat = firstLine.split("\\s+").length >= 3;
            }

            // 清空原有数据
            localDataList.clear();
            tcsIdPortMap.clear();

            // 根据格式解析数据
            for (String line : lines) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 1) { // 至少有一列
                    String id = parts[0]; // 第一列为ID
                    localDataList.add(id);

                    // 如果是多列格式且有端口信息，则保存映射
                    if (isMultiColumnFormat && parts.length >= 3) {
                        String port = parts[2];
                        tcsIdPortMap.put(id, port);
                    }
                }
            }

            // 更新UI
            SwingUtilities.invokeLater(() -> {
                suggestionModel.clear();
                localDataList.forEach(suggestionModel::addElement);
                tcsSuggestionModel.clear();
                localDataList.forEach(tcsSuggestionModel::addElement);

                // MES模式下隐藏端口标签
                if (!isMES) {
                    updatePortLabel();
                } else {
                    portLabel.setText(""); // 清空端口标签
                }
            });

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "加载本地数据失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupLayout() {
        JPanel sessionPanel = new JPanel(new BorderLayout());
        sessionPanel.setBorder(BorderFactory.createTitledBorder("会话管理"));
        sessionPanel.setMinimumSize(new Dimension(300, 400));
        // 修改状态面板，显示状态点和当前用户
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(sessionStatusDot);
        statusPanel.add(new JLabel("连接状态:"));
        statusPanel.add(currentUserLabel); // 添加用户标签
        sessionPanel.add(statusPanel, BorderLayout.NORTH);
        sessionPanel.add(new JScrollPane(sessionList), BorderLayout.CENTER);
        statusPanel.add(modeSwitchBtn);

        JPanel sessionBtnPanel = new JPanel();
        sessionBtnPanel.add(addSessionBtn);
        sessionBtnPanel.add(removeSessionBtn);

        JPanel sessionDetailsPanel = new JPanel(new BorderLayout());
        sessionDetailsPanel.add(sessionBtnPanel, BorderLayout.SOUTH);
        // 端口信息面板
        portInfoPanel = new JPanel(new GridBagLayout());
        portInfoPanel.setBorder(BorderFactory.createTitledBorder("端口信息"));
        portInfoPanel.setMinimumSize(new Dimension(300, 100));


        leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sessionPanel, sessionDetailsPanel);
        leftSplitPane.setDividerLocation(400);

// 搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder(isMES ? "选择TX ID" : "选择机台ID"));

// 搜索标签和输入框
        searchPanel.add(new JLabel(isMES ? "TX ID:" : "机台ID:"));
        searchField.setPreferredSize(new Dimension(150, 24));
        searchPanel.add(searchField);

// 日期标签和选择器
        //searchPanel.add(dateLabel);
        searchPanel.add(isMES ? timeLabel : dateLabel);
        dateSpinner.setPreferredSize(new Dimension(120, 24));
        timesp.setPreferredSize(new Dimension(120, 24));
        searchPanel.add(isMES ? timesp : dateSpinner);
        if (isMES) {
            timesp.setText(getSelectedDateTime());
        }
// 分类按钮
        varBtn.setPreferredSize(new Dimension(60, 24));

        if(!isMES)
            searchPanel.add(varBtn);

// 搜索按钮
        searchBtn.setPreferredSize(new Dimension(80, 24));
        searchPanel.add(searchBtn);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.add(new JLabel("使用编辑器:"));
        actionPanel.add(editorComboBox);
        actionPanel.add(openBtn);

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("搜索结果"));
        resultPanel.add(searchPanel, BorderLayout.NORTH);
        resultPanel.add(resultScrollPane, BorderLayout.CENTER);
        resultPanel.add(actionPanel, BorderLayout.SOUTH);

        downloadPathLabel = new JLabel("下载路径: "+ downloadPath);
        chooseDownloadPathBtn = new JButton("选择下载路径");
        JPanel downloadPathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        downloadPathPanel.add(downloadPathLabel);
        downloadPathPanel.add(chooseDownloadPathBtn);

        resultPanel.add(downloadPathPanel, BorderLayout.SOUTH);

        JPanel leftMainPanel = new JPanel(new BorderLayout());
        leftMainPanel.add(sessionPanel, BorderLayout.CENTER);
        leftMainPanel.add(portInfoPanel, BorderLayout.SOUTH); // 添加端口信息面板

        // ... [原有代码] ...

        // 修改主分割面板的左侧组件
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftMainPanel, sessionDetailsPanel),
                resultPanel);
        mainSplitPane.setDividerLocation(580);


        // 新增机台ID/TX ID面板
        if (!isMES) { // 仅TCS模式显示机台ID连接
            JPanel tcsPanel = new JPanel(new GridBagLayout());
            tcsPanel.setBorder(BorderFactory.createTitledBorder("机台连接"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(2, 5, 2, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // 机台ID标签
            gbc.gridx = 0;
            gbc.gridy = 0;
            tcsPanel.add(new JLabel("机台ID:"), gbc);

            // 机台ID输入框
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 0.5;
            tcsIdField.setMinimumSize(new Dimension(150, 24));
            tcsPanel.add(tcsIdField, gbc);

            // 端口标签
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            portLabel.setPreferredSize(new Dimension(80, 24));
            tcsPanel.add(portLabel, gbc);

            // 连接TCS按钮
            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.NONE;
            connectTcsBtn.setPreferredSize(new Dimension(100, 24));
            tcsPanel.add(connectTcsBtn, gbc);


            // 机台状态
            gbc.gridx = 0;
            gbc.gridy = 1;
            tcsPanel.add(new JLabel("机台状态:"), gbc);


            // 机台状态显示框
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 0.5;
            eqpStateField.setMinimumSize(new Dimension(150, 24));
            eqpStateField.setEditable(false);
            tcsPanel.add(eqpStateField, gbc);


            gbc.gridx = 2;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            eqpStatusDot.setPreferredSize(new Dimension(90, 24));
            eqpStatusDot.setBorder(BorderFactory.createEtchedBorder());
            tcsPanel.add(eqpStatusDot, gbc);

            // 可视化连接按钮
            gbc.gridx = 3;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            VisualBtn.setPreferredSize(new Dimension(90, 24));
            tcsPanel.add(VisualBtn, gbc);

            sessionPanel.add(tcsPanel, BorderLayout.SOUTH); // 机台连接面板仍在下方

        }

        add(mainSplitPane);
    }

    // 更新连接状态和用户信息
    private void updateStatus(boolean isConnected, String user) {
        sessionStatusDot.setBackground(isConnected ? CONNECTED_COLOR : DISCONNECTED_COLOR);
        sessionStatusDot.setOpaque(true);
        if (isConnected && user != null) {
            currentUserLabel.setText("已连接 (" + user + ")");
        } else {
            currentUserLabel.setText("未连接");
        }
    }

    private void setupListeners() {
        sessionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    SessionConfig selected = sessionList.getSelectedValue();
                    if (selected != null) {
                        hostField.setText(selected.getHost());
                        portField.setText(String.valueOf(selected.getPort()));
                        userField.setText(selected.getUser());
                        passwordFieldActual.setText(selected.getPassword());
                    }
                }
            }
        });

        removeSessionBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSession();
            }
        });

        connectBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnectPreviousSession();
                performConnect();
            }
        });

        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                search();
            }
        });


        openBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSelectedFile();
            }
        });

        addSessionBtn.addActionListener(e -> new AddSessionDialog(this).setVisible(true));

        // 搜索输入框键盘事件监听
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_DOWN) {
                    if (suggestionModel.getSize() > 0) {
                        suggestionList.setSelectedIndex(0);
                        suggestionPopup.show(searchField, 0, searchField.getHeight());
                        e.consume();
                    }
                } else if (keyCode == KeyEvent.VK_ESCAPE) {
                    suggestionPopup.setVisible(false);
                } else {
                    suggestionPopup.setVisible(false);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                String input = searchField.getText().trim();
                filterTimer.cancel();
                filterTimer = new Timer();
                filterTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        filterSuggestions(input);
                        SwingUtilities.invokeLater(() -> {
                            if (suggestionModel.getSize() > 0 && searchField.hasFocus()) {
                                suggestionPopup.show(searchField, 0, searchField.getHeight());
                            } else {
                                suggestionPopup.setVisible(false);
                            }
                        });
                    }
                }, 0);
            }
        });

        resultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedFile();
                }
            }
        });

        sessionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    disconnectPreviousSession();
                    performConnect();
                }
            }
        });

        // 备选列表点击事件
        suggestionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && suggestionList.getSelectedValue() != null) {
                    String selected = suggestionList.getSelectedValue();
                    searchField.setText(selected);
                    suggestionPopup.setVisible(false);
                    searchField.requestFocusInWindow();
                }
            }
        });

        chooseDownloadPathBtn.addActionListener(e -> chooseDownloadPath());

        // 机台ID/TX ID输入框键盘事件监听（仅TCS模式）
        if (!isMES) {
            tcsIdField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    int keyCode = e.getKeyCode();
                    if (keyCode == KeyEvent.VK_DOWN) {
                        if (tcsSuggestionModel.getSize() > 0) {
                            tcsSuggestionList.setSelectedIndex(0);
                            tcsSuggestionPopup.show(tcsIdField, 0, tcsIdField.getHeight());
                            e.consume();
                        }
                    } else if (keyCode == KeyEvent.VK_ESCAPE) {
                        tcsSuggestionPopup.setVisible(false);
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    String input = tcsIdField.getText().trim();
                    tcsFilterTimer.cancel();
                    tcsFilterTimer = new Timer();
                    tcsFilterTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            filterTcsSuggestions(input);
                            SwingUtilities.invokeLater(() -> {
                                if (tcsSuggestionModel.getSize() > 0 && tcsIdField.hasFocus()) {
                                    tcsSuggestionPopup.show(tcsIdField, 0, tcsIdField.getHeight());
                                } else {
                                    tcsSuggestionPopup.setVisible(false);
                                }
                            });
                        }
                    }, 300);
                }
            });

            // 备选列表点击事件
            tcsSuggestionList.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting() && tcsSuggestionList.getSelectedValue() != null) {
                        String selected = tcsSuggestionList.getSelectedValue();
                        tcsIdField.setText(selected);
                        tcsSuggestionPopup.setVisible(false);
                        tcsIdField.requestFocusInWindow();
                    }
                }
            });

            tcsIdField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updatePortLabel();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updatePortLabel();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updatePortLabel();
                }
            });



            connectTcsBtn.addActionListener(e -> connectToTcs());
            VisualBtn.addActionListener(e -> tcsVisual());
            // 日期输入框监听，确保输入格式正确
            // 日期选择器监听，确保按天调整
            dateSpinner.addChangeListener(e -> {
                SpinnerDateModel model = (SpinnerDateModel) dateSpinner.getModel();
                model.setCalendarField(Calendar.DAY_OF_MONTH); // 确保调整的是日期
            });

            // 日期输入框焦点事件，确保格式正确
            dateField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    try {
                        dateSpinner.commitEdit();
                    } catch (java.text.ParseException ex) {
                        dateSpinner.setValue(new Date());
                    }
                }
            });
            varBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    executePythonScript();

                }
            });
        }
        modeSwitchBtn.addActionListener(e -> restartApplication()); // 移到此处
    }
    private void switchMode() {
        disconnectPreviousSession();
        isMES = !isMES;
        CONFIG_FILE = isMES ? MES_CONFIG_FILE : TCS_CONFIG_FILE;
        setTitle(isMES ? "MES实用小工具" : "TCS实用小工具");
        portInfoPanel.setVisible(false);
        sessionListModel.clear();
        sessionConfigs.clear();
        loadSessions();

        loadLocalData(isMES ? "txList.txt" : "tcsxList.txt");

        searchResults.clear();
        resultListModel.clear();

        updateUILayout();

        if (isMES) {
            searchField.requestFocusInWindow();
        } else {
            tcsIdField.requestFocusInWindow();
        }
    }

    private void updateUILayout() {
        // 显示/隐藏机台ID面板
        for (Component comp : sessionPanel.getComponents()) {
            if (comp instanceof JPanel &&
                    ((JPanel) comp).getBorder() instanceof TitledBorder &&
                    ((TitledBorder) ((JPanel) comp).getBorder()).getTitle().equals("机台ID连接")) {
                comp.setVisible(!isMES);
                break;
            }
        }

        // 更新搜索面板标题
        TitledBorder searchBorder = (TitledBorder) searchPanel.getBorder();
        searchBorder.setTitle(isMES ? "选择TX ID" : "选择机台ID");

        // 更新搜索标签
        ((JLabel) searchPanel.getComponent(0)).setText(isMES ? "TX ID:" : "机台ID:");

        // 更新连接TCS按钮可见性
        connectTcsBtn.setVisible(!isMES);
        portInfoPanel.setVisible(!isMES);
        // 重绘界面
        validate();
        repaint();
    }
    private void updatePortLabel() {
        if (isMES) return; // MES模式不显示端口

        String tcsId = tcsIdField.getText().trim();
        String port = tcsIdPortMap.getOrDefault(tcsId, "");
        portLabel.setText("端口: " + (port.isEmpty() ? "未知" : port));
    }

    private void filterTcsSuggestions(String keyword) {
        List<String> filtered = localDataList.stream()
                .filter(item -> item.toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        SwingUtilities.invokeLater(() -> {
            tcsSuggestionModel.clear();
            filtered.forEach(tcsSuggestionModel::addElement);
        });
    }

    private List<Process> runningProcesses = new ArrayList<>();


    private void connectToTcs() {
        if (isMES) return; // MES模式不支持此功能

        String tcsId = tcsIdField.getText().trim();
        String port = tcsIdPortMap.get(tcsId);

        if (tcsId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入机台ID！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (port == null || port.isEmpty()) {
            JOptionPane.showMessageDialog(this, "未找到对应的端口号！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // 使用批处理文件中的Java路径
            String javaExe = "\"C:\\Program Files (x86)\\IBM\\Java80\\jre\\bin\\javaw.exe\"";

            // 使用完整的JAR文件路径
            String jarPath = "\"C:\\Program Files (x86)\\IBM\\TCSX Java Client\\jtcsxc.jar\"";

            // 构建完整命令（带引号处理空格）
            String command = javaExe + " -jar " + jarPath + " -admin 10.196.60.107 " + port;

            // 执行命令
            Process process = Runtime.getRuntime().exec(command);

            // 添加到进程列表
            runningProcesses.add(process);

            // 记录启动信息
            System.out.println("已启动客户端，机台ID: " + tcsId + "，端口: " + port);
            System.out.println("执行命令: " + command);

            // 显示成功消息
            JOptionPane.showMessageDialog(this,
                    "已启动客户端: " + tcsId + " (端口: " + port + ")\n" +
                            "当前已启动 " + runningProcesses.size() + " 个客户端实例",
                    "成功",
                    JOptionPane.INFORMATION_MESSAGE);

            // 异步处理进程结束事件
            new Thread(() -> {
                try {
                    int exitCode = process.waitFor();
                    System.out.println("客户端进程已结束，机台ID: " + tcsId + "，退出码: " + exitCode);
                    runningProcesses.remove(process);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "启动客户端失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void chooseDownloadPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(downloadPath));
        chooser.setDialogTitle("选择下载目录");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            downloadPath = chooser.getSelectedFile().getAbsolutePath();
            downloadPathLabel.setText("下载路径: " + downloadPath);
            saveDownloadPath();
        }
    }

    private void loadDownloadPath() {
        File file = new File(DOWNLOAD_CONFIG_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                downloadPath = reader.readLine();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "加载下载路径失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            downloadPath = TEMP_DIR;
        }
    }

    private void saveDownloadPath() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DOWNLOAD_CONFIG_FILE))) {
            writer.write(downloadPath);
            downloadPathLabel.setText("下载路径: " + downloadPath);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "保存下载路径失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }




    private void filterSuggestions(String keyword) {
        List<String> filtered = localDataList.stream()
                .filter(item -> item.toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        SwingUtilities.invokeLater(() -> {
            suggestionModel.clear();
            filtered.forEach(suggestionModel::addElement);
        });
    }


    private void removeSession() {
        SessionConfig selected = sessionList.getSelectedValue();
        if (selected != null) {
            sessionConfigs.remove(selected);
            sessionListModel.removeElement(selected);
            saveSessions();
        }
    }

    private void performConnect() {
        String host = hostField.getText();
        int port = Integer.parseInt(portField.getText());
        String user = userField.getText();
        String password = new String(passwordFieldActual.getPassword());

        try {
            JSch jsch = new JSch();
            currentSession = jsch.getSession(user, host, port);

            if (!password.isEmpty()) {
                currentSession.setPassword(password);
            }

            currentSession.setConfig("StrictHostKeyChecking", "no");
            currentSession.connect(30000);

            JOptionPane.showMessageDialog(this, "连接成功！");
            updateStatus(true, user);
        } catch (JSchException ex) {
            JOptionPane.showMessageDialog(this, "连接失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            updateStatus(false, null);
            ex.printStackTrace();
        }
    }

    private void disconnectPreviousSession() {
        if (currentSession != null && currentSession.isConnected()) {
            currentSession.disconnect();
            currentSession = null;
            updateStatus(false, null);
        }
    }

    private void search() {
        if (currentSession == null || !currentSession.isConnected()) {
            JOptionPane.showMessageDialog(this, "请先连接到服务器！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SessionConfig selectedConfig = sessionList.getSelectedValue();
        if (selectedConfig == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个会话！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (timesp.getText().equals(""))
        {
            JOptionPane.showMessageDialog(this, "请输入时间！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String searchPath_TCS  = selectedConfig.getUserHome() + "tcsx/log/";
        String searchPath_MES  = selectedConfig.getUserHome() + "wfview_app/mm/applog/";

        String searchText;
        if (!isMES)
            searchText = searchField.getText().trim();
        else
            searchText = searchField.getText().trim() + "_markpoint" + timesp.getText().trim();

        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    isMES ? "请输入TX ID！" : "请输入机台ID！", // 根据模式调整提示
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            resultListModel.clear();
            resultListModel.addElement("正在搜索...");
            searchResults.clear();
        });


        new Thread(() -> {
            try {
                String escapedSearchText = searchText.replace("\"", "\\\"").replace("*", "\\*");
                String searchPattern = escapedSearchText;

                if (!searchText.contains("*")) {
                    searchPattern = "*" + escapedSearchText + "*";
                }
                String command = " ";
                if (!isMES) {
                     command = "find " + searchPath_TCS + " -name \"" + searchPattern + "\" -type f 2>/dev/null";
                }
                else {
                    // MES模式下，同时搜索带时间戳的日志和最新日志
                    String timeBasedLogPattern = "\"" + searchPattern + "\"";
                    String latestLogPattern = "\"" + searchField.getText().trim() + "_markpoint.log\"";

                    // 使用OR逻辑组合两个搜索条件
                    command = "find " + searchPath_MES + " \\( -name " + timeBasedLogPattern + " -o -name " + latestLogPattern + " \\) -type f";
                }
//                else {
//                    command = "find " + searchPath_MES + " -name \"" + searchPattern + "\"";
//                }
               // System.out.println("执行命令: " + command);

                Channel channel = currentSession.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                channel.setInputStream(null);
                ((ChannelExec) channel).setErrStream(System.err);

                InputStream in = channel.getInputStream();
                channel.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                List<String> tempResults = new ArrayList<>();
                String line;
                int filteredCount = 0;

                while ((line = reader.readLine()) != null) {
                    if (!line.contains("comm")) {
                        tempResults.add(line);
                    } else {
                        filteredCount++;
                    }
                }

                channel.disconnect();

                final List<String> finalResults = tempResults;
                final int filtered = filteredCount;

                SwingUtilities.invokeLater(() -> {
                    resultListModel.clear();
                    searchResults.clear();

                    if (finalResults.isEmpty()) {
                        String message = "未找到匹配的文件";
                        if (filtered > 0) {
                            message += "（过滤掉 " + filtered + " 个包含'comm'的文件）";
                        }
                        resultListModel.addElement(message);
                        JOptionPane.showMessageDialog(this, message, "提示", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        for (String result : finalResults) {
                            resultListModel.addElement(result);
                            searchResults.add(result);
                        }
                        System.out.println("找到 " + searchResults.size() + " 个文件，过滤掉 " + filtered + " 个");
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    resultListModel.clear();
                    resultListModel.addElement("搜索过程中发生错误");
                    resultListModel.addElement(ex.getMessage());
                    JOptionPane.showMessageDialog(this, "搜索失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                });
            }
        }).start();
    }

    private void openSelectedFile() {
        int selectedIndex = resultList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= searchResults.size()) {
            JOptionPane.showMessageDialog(this, "请先选择一个文件！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String remoteFilePath = searchResults.get(selectedIndex);
        String fileName = new File(remoteFilePath).getName();
        String localFilePath = downloadPath + File.separator + fileName;

        new Thread(() -> {
            try {
                downloadFile(remoteFilePath, localFilePath);
                String editor = (String) editorComboBox.getSelectedItem();
                openLocalFile(localFilePath, editor);
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "打开文件失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                });
            }
        }).start();
    }

    private void downloadFile(String remoteFilePath, String localFilePath) throws JSchException, SftpException, IOException {
        ChannelSftp sftpChannel = (ChannelSftp) currentSession.openChannel("sftp");
        sftpChannel.connect();

        try (OutputStream out = new FileOutputStream(localFilePath)) {
            sftpChannel.get(remoteFilePath, out);
        } finally {
            sftpChannel.disconnect();
        }
    }

    private void openLocalFile(String filePath, String editor) throws IOException {
        File file = new File(filePath);

        if ("系统默认".equals(editor)) {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                Runtime.getRuntime().exec("open " + filePath);
            }
        } else if ("记事本".equals(editor)) {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                Runtime.getRuntime().exec("notepad " + filePath);
            } else {
                JOptionPane.showMessageDialog(this, "记事本仅适用于Windows系统", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else if ("VS Code".equals(editor)) {
            Runtime.getRuntime().exec("code " + filePath);
        } else if ("Sublime Text".equals(editor)) {
            Runtime.getRuntime().exec("subl " + filePath);
        } else {
            JOptionPane.showMessageDialog(this, "未知编辑器: " + editor, "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSessions() {
        if(isMES)
        {
            CONFIG_FILE = MES_CONFIG_FILE;
        }
        else {
            CONFIG_FILE = TCS_CONFIG_FILE;
        }
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                sessionConfigs = (List<SessionConfig>) ois.readObject();
                for (SessionConfig config : sessionConfigs) {
                    sessionListModel.addElement(config);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "加载会话失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void saveSessions() {
        if(isMES)
        {
            CONFIG_FILE = MES_CONFIG_FILE;
        }
        else {
            CONFIG_FILE = TCS_CONFIG_FILE;
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CONFIG_FILE))) {
            oos.writeObject(sessionConfigs);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "保存会话失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 新增启动选项窗口
        String[] options = {"TCS", "MES"};
        int choice = JOptionPane.showOptionDialog(null,
                "选择系统模式",
                "模式选择",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        SwingUtilities.invokeLater(() -> {
            boolean isMES = (choice == 1);
            new AixLogFinder(isMES).setVisible(true);
        });
    }

    public static class SessionConfig implements Serializable {
        private static final long serialVersionUID = 1L;
        private String host;
        private int port;
        private String user;
        private String password;

        public SessionConfig(String host, int port, String user, String password) {
            this.host = host;
            this.port = port;
            this.user = user;
            this.password = password;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }

        public String getUserHome() {
            return "/home/" + user + "/";
        }

        @Override
        public String toString() {
            return user + "@" + host + ":" + port;
        }
    }

    public void addSessionToModel(SessionConfig config) {
        sessionConfigs.add(config);
        sessionListModel.addElement(config);
        saveSessions();
    }
    private void restartApplication() {
        // 1. 终止后台线程
        terminateBackgroundThreads();

        // 2. 异步保存资源
        new Thread(this::saveResourcesBeforeExit).start();

        // 3. 延迟关闭当前窗口（确保新进程启动）
        new Thread(() -> {
            try {
                Thread.sleep(100); // 等待资源保存
                SwingUtilities.invokeLater(() -> {
                    dispose(); // 关闭UI
                    System.exit(0); // 退出JVM
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        // 4. 启动新进程（独立线程）
        new Thread(this::startNewProcess).start();
    }

    private void terminateBackgroundThreads() {
        filterTimer.cancel();
        tcsFilterTimer.cancel();
        // 终止其他自定义线程
        runningProcesses.forEach(Process::destroyForcibly);
    }

    private void saveResourcesBeforeExit() {
        saveSessions();
        saveDownloadPath();
        // 其他轻量级保存操作
    }

    private void startNewProcess() {
        try {
            String javaPath = System.getProperty("java.home") + "/bin/java";
            String classpath = System.getProperty("java.class.path");
            String mainClass = getClass().getName();

            ProcessBuilder builder = new ProcessBuilder(
                    javaPath, "-cp", classpath, mainClass
            );
            builder.inheritIO();
            builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void tcsVisual() {
        String tcsId = tcsIdField.getText().trim();
        String sql = "";
        DTCSX_EQP eqp = new DTCSX_EQP();

        if (tcsId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入机台ID！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        switch (1) {
            case 1: // Read DTCSX_EQP
                sql = MessageFormat.format("SELECT * FROM TCSX.DTCSX_EQP where eqpid = ''{0}''", tcsId);
                List<DTCSX_EQP> eqpList = executeQuery(sql, DTCSX_EQP.class);
                if (!eqpList.isEmpty()) {
                    eqp = eqpList.get(0);
                    eqpStateField.setText(eqp.getMode().toString());
                    eqpStatusDot.setText(eqp.getCtrlmode().startsWith("OFF") ?
                            eqp.getCtrlmode() : eqp.getCtrlmode().toString().split("-")[1]);
                    updateEQPStatusDot(eqp.getMode().startsWith("M"));
                }
               // break;

            case 2: // Read DTCSX_PORT
                sql = MessageFormat.format("SELECT * FROM TCSX.DTCSX_PORT where eqpid = ''{0}''", tcsId);
                List<DTCSX_PORT> portList = executeQuery(sql, DTCSX_PORT.class);

                // 在EDT线程中更新UI
                SwingUtilities.invokeLater(() -> {
                    // 清空原有端口信息
                    portInfoPanel.removeAll();
                    portInfoPanel.setLayout(new GridBagLayout());
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.insets = new Insets(2, 5, 2, 5);
                    gbc.anchor = GridBagConstraints.WEST;
                    gbc.fill = GridBagConstraints.HORIZONTAL;

                    if (portList.isEmpty()) {
                        // 显示无端口信息
                        gbc.gridx = 0;
                        gbc.gridy = 0;
                        gbc.gridwidth = GridBagConstraints.REMAINDER;
                        portInfoPanel.add(new JLabel("未查询到端口信息"), gbc);
                    } else {
                        // 添加表头
                        gbc.gridy = 0;
                        gbc.gridwidth = 1;
                        gbc.weightx = 0.2;

                        // 表头数组
                        String[] headers = {"PortID", "Port类型", "Port状态", "片盒ID", "更新时间"};
                        for (int i = 0; i < headers.length; i++) {
                            gbc.gridx = i;
                            portInfoPanel.add(createBoldLabel(headers[i]), gbc);
                        }
                        // 显示所有端口信息
                        for (int i = 0; i < portList.size(); i++) {
                            DTCSX_PORT port = portList.get(i);
                            int row = i + 1;

                            // PortID
                            gbc.gridx = 0;
                            gbc.gridy = row;
                            portInfoPanel.add(new JLabel(port.getPortid()), gbc);

                            // Port类型
                            gbc.gridx = 1;
                            portInfoPanel.add(new JLabel(port.getPorttype()), gbc);

                            // Port状态
                            gbc.gridx = 2;
                            portInfoPanel.add(new JLabel(port.getState()), gbc);

                            // 载体ID
                            gbc.gridx = 3;
                            portInfoPanel.add(new JLabel(port.getCarid()), gbc);

                            // 状态更新时间
                            gbc.gridx = 4;
                            portInfoPanel.add(new JLabel(
                                    port.getStateupdate() != null ?
                                            port.getStateupdate().toString() : "未知"), gbc);
                        }
                    }

                    // 设置面板可见并更新布局
                    portInfoPanel.setVisible(true);
                    portInfoPanel.revalidate();
                    portInfoPanel.repaint();
                });


            case 3:
                // 后续扩展
                break;
        }
    }

    private void updateEQPStatusDot(boolean isTrue) {
        eqpStatusDot.setBackground(isTrue ? DISCONNECTED_COLOR : CONNECTED_COLOR);
        eqpStatusDot.setOpaque(true);
    }
    // 辅助方法：创建加粗标签
    private JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        Font font = label.getFont();
        label.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
        return label;
    }

    private void executePythonScript() {
        if (currentSession == null || !currentSession.isConnected()) {
            JOptionPane.showMessageDialog(this, "请先连接到服务器！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SessionConfig selectedConfig = sessionList.getSelectedValue();
        if (selectedConfig == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个会话！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 获取用户输入的参数
        String machineId =  searchField.getText().trim() ;
        String date = getSelectedDate(); // 需要实现获取日期的方法，例如从日期选择器获取

        if (machineId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    isMES ? "请输入TX ID！" : "请输入机台ID！",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

       if (date.isEmpty()) {
           JOptionPane.showMessageDialog(this, "请选择日期！", "提示", JOptionPane.WARNING_MESSAGE);
           return;
       }

        // 更新UI显示执行状态
        SwingUtilities.invokeLater(() -> {
            resultListModel.clear();
            resultListModel.addElement("正在执行脚本...");
        });

        new Thread(() -> {
            try {
                // 构建并执行Python脚本命令
                String escapedMachineId = escapeShellArg(machineId);
                String escapedDate = escapeShellArg(date);

                String scriptPath = "~/tcsx/sh/MATool_LogMerge.py";
                String command = "/opt/freeware/bin/python3 " + scriptPath + " "
                        + escapedMachineId + " " + escapedDate;

                System.out.println("执行命令: " + command);

                // 执行命令并获取输出
                Channel channel = currentSession.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                channel.setInputStream(null);
                ((ChannelExec) channel).setErrStream(System.err);

                InputStream in = channel.getInputStream();
                channel.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                List<String> outputLines = new ArrayList<>();
                String line;

                while ((line = reader.readLine()) != null) {
                    outputLines.add(line);
                }

                channel.disconnect();

                // 更新UI显示结果
                SwingUtilities.invokeLater(() -> {
                    resultListModel.clear();

                    if (outputLines.isEmpty()) {
                        resultListModel.addElement("脚本执行完成，但没有输出结果");
                    } else {
                        for (String outputLine : outputLines) {
                            resultListModel.addElement(outputLine);
                        }
                    }

                    JOptionPane.showMessageDialog(
                            this,
                            "脚本执行完成，返回 " + outputLines.size() + " 行结果",
                            "成功",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    search();
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    resultListModel.clear();
                    resultListModel.addElement("执行脚本时发生错误");
                    resultListModel.addElement(ex.getMessage());
                    JOptionPane.showMessageDialog(
                            this,
                            "执行脚本失败: " + ex.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE
                    );
                    ex.printStackTrace();
                });
            }
        }).start();
    }
    // 安全转义shell参数的辅助方法
    private String escapeShellArg(String arg) {
        if (arg == null) return "";
        return "'" + arg.replace("'", "'\\''") + "'";
    }
    // 获取选定日期的方法（示例实现）
    private String getSelectedDate() {
        try {
            Date date = (Date) dateSpinner.getValue();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            return sdf.format(date);
        } catch (Exception e) {
            // 格式错误时返回当前日期
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            return sdf.format(today);
        }
    }

    private String getSelectedDateTime() {
        try {
            Date date = (Date) dateSpinner.getValue();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH"); // 格式：年月日小时（24小时制）
            return sdf.format(date);
        } catch (Exception e) {
            // 格式错误时返回当前日期时间
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
            return sdf.format(today);
        }
    }
}

