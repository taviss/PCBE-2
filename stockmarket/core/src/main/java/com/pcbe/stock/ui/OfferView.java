package com.pcbe.stock.ui;

import com.pcbe.stock.event.Offer;

import javax.swing.*;
import java.awt.*;

public class OfferView extends JPanel implements ListCellRenderer<Offer> {

    private static final long serialVersionUID = 1L;
    private JLabel label1 = new JLabel();
    private JLabel label2 = new JLabel();
    private JLabel label3 = new JLabel();
    private JLabel label4 = new JLabel();
    private JLabel label5 = new JLabel();
    private JLabel label6 = new JLabel();
    private JLabel label7 = new JLabel();
    private JPanel textPanel;

    public OfferView() {
        setOpaque(true);
        textPanel = new JPanel(new GridLayout(0, 1));
        textPanel.setOpaque(true);
        this.setLayout(new BorderLayout(7, 7));
        textPanel.add(label1);
        textPanel.add(label2);
        textPanel.add(label3);
        textPanel.add(label4);
        textPanel.add(label5);
        textPanel.add(label7);
        textPanel.add(label6);
        label6.setVisible(false);
        this.add(textPanel, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Offer> list, Offer offer, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        label1.setText("Title : " + offer.getTitle());
        label2.setText("Company : " + offer.getCompany());
        label3.setText("Price : " + offer.getPrice());
        label4.setText("Creation date : " + offer.getCreationDate());
        label5.setText("Last changed : " + offer.getLastChanged().toString());
        label6.setText("Views : " + offer.getViews());
        label7.setText("Highest Bidder : " + offer.getHighestBidder());
        if(offer.areViewsVisible()) {
            label6.setVisible(true);
        }else {
            label6.setVisible(false);
        }
        
        if(isSelected) {
            textPanel.setBackground(Color.ORANGE);
        } else {
            textPanel.setBackground(this.getBackground());
        }
        return this;
    }

}
