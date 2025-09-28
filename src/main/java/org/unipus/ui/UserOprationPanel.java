package org.unipus.ui;

/* (っ*´Д`)っ 小代码要被看光啦 */

import org.unipus.web.response.CourseListResponse;

import javax.swing.*;
import java.util.List;

public class UserOprationPanel extends JPanel {

    public void initChooseCoursePanel(List<CourseListResponse.Course> courses, ChooseCoursePanel.ConfirmListener listener) {
        ChooseCoursePanel chooseCoursePanel = new ChooseCoursePanel(courses, listener);
        SwingUtilities.invokeLater(() -> {
            removeAll();
            add(chooseCoursePanel);
            revalidate();
            repaint();
        });
    }
}
