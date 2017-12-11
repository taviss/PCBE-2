package com.pcbe.stock.seller.ui;

import com.pcbe.stock.event.Offer;
import com.pcbe.stock.seller.StockPublisher;
import com.pcbe.stock.ui.OfferView;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.jms.JMSException;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class StockPublisherGUI {
    
    private String company;
    private StockPublisher listener;
    private JList<Offer> offerList;
    private DefaultListModel<Offer> model;

    public StockPublisherGUI(String company) {
        this.company = company;
        initGUI();
    }

    private JPanel initContent() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        JButton editOfferButton = new JButton("Edit selected offer");
        editOfferButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Offer selectedOffer = offerList.getSelectedValue();
                if (selectedOffer != null) {
                    JPanel fields = new JPanel(new GridLayout(4, 2));
                    JLabel label1 = new JLabel("Title:");
                    JLabel label2 = new JLabel("Company:");
                    JLabel label3 = new JLabel("Price:");
                    JLabel label4 = new JLabel("Description:");
                    JTextField field1 = new JTextField(10);
                    JTextField field2 = new JTextField(10);
                    JTextField field3 = new JTextField(10);
                    JTextField field4 = new JTextField(40);
                    field1.setText(selectedOffer.getTitle());
                    field2.setText(selectedOffer.getCompany());
                    field2.setEnabled(false);
                    field3.setText(selectedOffer.getPrice() + "");
                    field4.setText(selectedOffer.getDescription());
                    fields.add(label1);
                    fields.add(field1);
                    fields.add(label2);
                    fields.add(field2);
                    fields.add(label3);
                    fields.add(field3);
                    fields.add(label4);
                    fields.add(field4);
                    int result = JOptionPane.showConfirmDialog(null, fields, "Offer Details",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (!(field1.getText().isEmpty()) && !(field3.getText().isEmpty())
                            && !(field4.getText().isEmpty())) {
                        switch (result) {
                            case JOptionPane.OK_OPTION:
                                try {
                                    selectedOffer.setTitle(field1.getText());
                                    selectedOffer.setDescription(field4.getText());
                                    selectedOffer.setPrice(Float.parseFloat(field3.getText()));
                                    selectedOffer.setLastChanged();
                                    int index = offerList.getSelectedIndex();
                                    ((DefaultListModel<Offer>) offerList.getModel()).remove(index);
                                    ((DefaultListModel<Offer>) model).add(index, selectedOffer);
                                    notifyEdited(selectedOffer);
                                    selectedOffer.setHighestBidder("");
                                } catch (NumberFormatException ex) {
                                    JOptionPane.showMessageDialog(null, "Price value is not a number.");
                                }
                                break;
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Fields cannot be empty.");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Select an offer first.");
                }
            }
        });
        /*
        JButton updateButton = new JButton("Get updates for selected offer");
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Offer selectedOffer = offerList.getSelectedValue();
                if (selectedOffer != null) {
                    //notifyFollow(selectedOffer.getId());
                    selectedOffer.setVisibleViews(true);
                    JOptionPane.showMessageDialog(null, "You will now see updates and views for this offer.");
                } else
                    JOptionPane.showMessageDialog(null, "Select an offer first.");
            }
        });*/
        JButton createOfferButton = new JButton("Create new offer");
        createOfferButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JPanel fields = new JPanel(new GridLayout(4, 2));
                JLabel label1 = new JLabel("Title:");
                JLabel label2 = new JLabel("Company:");
                JLabel label3 = new JLabel("Price:");
                JLabel label4 = new JLabel("Description:");
                JTextField field1 = new JTextField(10);
                JTextField field2 = new JTextField(10);
                JTextField field3 = new JTextField(10);
                JTextField field4 = new JTextField(40);
                field2.setText(company);
                field2.setEnabled(false);
                fields.add(label1);
                fields.add(field1);
                fields.add(label2);
                fields.add(field2);
                fields.add(label3);
                fields.add(field3);
                fields.add(label4);
                fields.add(field4);
                int result = JOptionPane.showConfirmDialog(null, fields, "Offer Details", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (!(field1.getText().isEmpty()) && !(field2.getText().isEmpty()) && !(field3.getText().isEmpty())
                        && !(field4.getText().isEmpty())) {
                    switch (result) {
                        case JOptionPane.OK_OPTION:
                            try {
                                Offer offer = new Offer(field1.getText(), field2.getText(),
                                        Float.parseFloat(field3.getText()), listener.getNextId(), field4.getText(), false);
                                ((DefaultListModel<Offer>) model).addElement(offer);
                                notifyAdd(offer);
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, "Price value is not a number.");
                            }
                            break;
                    }
                } else
                    JOptionPane.showMessageDialog(null, "Fields cannot be empty.");
            }
        });
        JButton closeOfferButton = new JButton("Accept(close) selected offer");
        closeOfferButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Offer selectedOffer = offerList.getSelectedValue();
                if (selectedOffer != null) {
                    int index = offerList.getSelectedIndex();
                    notifyDeleted(selectedOffer);
                    ((DefaultListModel<Offer>) offerList.getModel()).remove(index);
                    JOptionPane.showMessageDialog(null, "Offer was deleted.");
                } else
                    JOptionPane.showMessageDialog(null, "Select an offer first.");
            }
        });

        JPanel offersPanel = new JPanel(new BorderLayout());
        offersPanel.add(new JScrollPane(offerList = createOfferList()), BorderLayout.CENTER);
        contentPanel.add(offersPanel);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(3, 1));
        buttonsPanel.add(editOfferButton, BorderLayout.WEST);
        //buttonsPanel.add(updateButton, BorderLayout.EAST);
        buttonsPanel.add(createOfferButton, BorderLayout.WEST);
        buttonsPanel.add(closeOfferButton, BorderLayout.EAST);
        contentPanel.add(buttonsPanel);
        contentPanel.setPreferredSize(new Dimension(500, 500));
        return contentPanel;
    }

    private JList<Offer> createOfferList() {
        model = new DefaultListModel<>();
        JList<Offer> offers = new JList<Offer>(model);
        offers.setCellRenderer(new OfferView());
        return offers;
    }

    private void initGUI() {
        JFrame mainFrame = new JFrame("Company: " + company);
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel contentPanel = initContent();
        mainPanel.add(contentPanel, BorderLayout.WEST);
        mainFrame.add(mainPanel);
        mainFrame.setResizable(true);
        mainFrame.pack();
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
    }

    public void setListener(StockPublisher listener) {
        this.listener = listener;
    }
    
    public void updateAllOffers() {
        for(int i = 0; i < model.getSize(); i++) {
            notifyEdited(model.getElementAt(i));
        }
    }

    public void updateOffer(Offer offerA) {
        int offerNumber = model.getSize();
        for (int i = 0; i < offerNumber; i++) {
            Offer offer = model.getElementAt(i);
            if (offerA.getId() == offer.getId()) {
                ((DefaultListModel<Offer>) model).remove(i);
                offer.increment();
                offer.setPrice(offerA.getPrice());
                offer.setHighestBidder(offerA.getHighestBidder());
                ((DefaultListModel<Offer>) model).insertElementAt(offer, i);
            }
            notifyEdited(offer);
            break;
        }
        
    }

    private void notifyEdited(Offer offer) {
        try {
            listener.updateOffer(offer);
        } catch (JMSException e) {
            //TODO
        }
    }

    private void notifyFollow(long offerId) {
        //listener.followRequest(offerId);
    }

    public void notifyAdd(Offer offer) {
        try {
            listener.createNewOffer(offer);
        } catch (JMSException e) {
            //TODO
        }
    }

    private void notifyDeleted(Offer offer) {
        try {
            listener.closeOffer(offer);
        } catch(JMSException e) {
            //TODO
        }
    }
}