package org.unipus.ui;

/* (っ*´Д`)っ 小代码要被看光啦 */

import javax.swing.*;
import java.awt.*;
import java.util.List;
import org.unipus.web.response.CourseListResponse.Course;
import org.unipus.web.response.CourseListResponse.CourseResource;

public class ChooseCoursePanel extends JPanel {
    private final JList<Course> courseList;
    private final DefaultListModel<Course> courseListModel;
    private final JList<CourseResource> resourceList;
    private final DefaultListModel<CourseResource> resourceListModel;
    private final JButton confirmButton;
    private Course selectedCourse;
    private CourseResource selectedResource;

    public ChooseCoursePanel(List<Course> courses, ConfirmListener listener) {
        setLayout(new BorderLayout());
        courseListModel = new DefaultListModel<>();
        for (Course c : courses) courseListModel.addElement(c);
        courseList = new JList<>(courseListModel);
        courseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Course) setText(((Course) value).getName());
                return this;
            }
        });

        resourceListModel = new DefaultListModel<>();
        resourceList = new JList<>(resourceListModel);
        resourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resourceList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CourseResource) setText(((CourseResource) value).getName());
                return this;
            }
        });

        courseList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedCourse = courseList.getSelectedValue();
                resourceListModel.clear();
                if (selectedCourse != null && selectedCourse.getCourseResourceList() != null) {
                    for (CourseResource r : selectedCourse.getCourseResourceList()) resourceListModel.addElement(r);
                }
            }
        });

        resourceList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedResource = resourceList.getSelectedValue();
            }
        });

        confirmButton = new JButton("确认选择");
        confirmButton.addActionListener(e -> {
            if (selectedCourse != null && selectedResource != null) {
                listener.onConfirm(selectedCourse, selectedResource);
            } else {
                JOptionPane.showMessageDialog(this, "请选择课程和教程！", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(courseList), new JScrollPane(resourceList));
        splitPane.setDividerLocation(200);
        add(splitPane, BorderLayout.CENTER);
        add(confirmButton, BorderLayout.SOUTH);
    }

    public interface ConfirmListener {
        void onConfirm(Course course, CourseResource resource);
    }
}
