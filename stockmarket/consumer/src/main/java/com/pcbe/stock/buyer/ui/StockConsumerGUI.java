package com.pcbe.stock.buyer.ui;

import com.pcbe.stock.buyer.StockConsumer;
import com.pcbe.stock.event.Offer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class StockConsumerGUI {

    private StockConsumer listener;
    private JList<Offer> offerList = new JList<Offer>();
    private DefaultListModel<Offer> model;
    private String username;
    private ArrayList<String> filters = new ArrayList<String>();
    private float minPrice = 0;
    private float maxPrice = 999999999;
    private DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    private Date date;
    private float price;

    public StockConsumerGUI(String username) {
        try {
            date = format.parse("1/1/1000");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.username=username;
        initGUI();
    }

    public void setListener(StockConsumer listener) {
        this.listener = listener;
    }

    private JPanel initContent() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        JButton viewButton = new JButton("View offer");
        viewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Offer selectedOffer = offerList.getSelectedValue();
                if (selectedOffer != null) {
                    JFrame bidFrame = new JFrame("Start bidding");
                    JPanel offerPanel = new JPanel();
                    offerPanel.setLayout(new BoxLayout(offerPanel, BoxLayout.Y_AXIS));
                    offerPanel.add(new JLabel("Description: " + selectedOffer.getDescription()));
                    JPanel inputPrice = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    inputPrice.add(new JLabel("Price:"));
                    JTextField priceField = new JTextField(10);
                    priceField.setText("");
                    priceField.addFocusListener(new FocusAdapter() {
                        public void focusLost(FocusEvent e) {
                            priceField.selectAll();
                            try {
                                price = Float.parseFloat(priceField.getText());
                            }catch(NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, "The price value is not a number.");
                            }
                        }
                    });
                    inputPrice.add(priceField);
                    offerPanel.add(inputPrice);
                    JButton bidButton = new JButton("Bid");
                    JButton cancelButton = new JButton("Cancel");
                    bidButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if(price<selectedOffer.getPrice()) {
                                JOptionPane.showMessageDialog(null, "Your offer was below the current price.");
                            } else {
                                selectedOffer.setPrice(price);
                                selectedOffer.setHighestBidder(username);
                            }
                            bidFrame.dispatchEvent(new WindowEvent(bidFrame, WindowEvent.WINDOW_CLOSING));
                            notifySeen(selectedOffer);
                            updateOffer(selectedOffer);
                        }
                    });
                    cancelButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            bidFrame.dispatchEvent(new WindowEvent(bidFrame, WindowEvent.WINDOW_CLOSING));
                            notifySeen(selectedOffer);
                        }
                    });
                    JPanel buttonsPanel = new JPanel();
                    buttonsPanel.setLayout(new GridLayout(2, 1));
                    buttonsPanel.add(bidButton, BorderLayout.WEST);
                    buttonsPanel.add(cancelButton, BorderLayout.EAST);
                    offerPanel.add(buttonsPanel);
                    bidFrame.add(offerPanel);
                    bidFrame.setVisible(true);
                    bidFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                    bidFrame.pack();

                } else {
                    JOptionPane.showMessageDialog(null, "Please select an offer.");
                }
            }
        });
        JButton filterButton = new JButton("Filter offers");
        filterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JPanel selectPanel = new JPanel(new GridLayout(2, 1));
                JTextField input = new JTextField(10);
                JComboBox<String> comboBox = new JComboBox<>(
                        new String[] { "Company", "MinPrice", "MaxPrice", "Oldest" });
                selectPanel.add(input);
                selectPanel.add(comboBox);
                int result = JOptionPane.showConfirmDialog(null, selectPanel, "Filter offers:",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                switch (result) {
                    case JOptionPane.OK_OPTION:
                        if(comboBox.getSelectedItem().equals("MinPrice")) {
                            try {
                                minPrice = Float.parseFloat(input.getText());
                            }catch(NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, "The price value is not a number.");
                            }
                        }
                        if(comboBox.getSelectedItem().equals("MaxPrice")) {
                            try {
                                maxPrice = Float.parseFloat(input.getText());
                            }catch(NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, "The price value is not a number.");
                            }
                        }
                        if(comboBox.getSelectedItem().equals("Oldest")) {
                            String string = input.getText();
                            try {
                                date = format.parse(string);
                            } catch (ParseException e1) {
                                JOptionPane.showMessageDialog(null, "Date format should be: dd/MM/yyyy");
                            }
                        }
                        //notifySubscribeTo(comboBox.getSelectedItem() + ":" + input.getText());
                        filters.add(comboBox.getSelectedItem() + ":" + input.getText());
                        break;
                }
            }
        });
        JButton showFiltersButton = new JButton("Show all filters");
        showFiltersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StringBuilder list = new StringBuilder();
                list.append("Your current filters are:"+System.lineSeparator());
                for(String filter : filters) {
                    list.append(filter + System.lineSeparator());
                }
                JOptionPane.showMessageDialog(null, list.toString());
            }
        });
        JButton removeFiltersButton = new JButton("Remove all filters");
        removeFiltersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                /*
                for(TopicSubscriber subscriber: subscribers) {
                    try {
                        subscriber.close();
                    } catch (JMSException e1) {
                        e1.printStackTrace();
                    }
                }*/
                minPrice = 0;
                maxPrice = 999999999;
                try {
                    date = format.parse("1/1/1000");
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
                //subscribers.removeAll(subscribers);
                filters.removeAll(filters);
            }
        });

        JPanel offersPanel = new JPanel(new BorderLayout());
        offersPanel.add(new JScrollPane(offerList = createOfferList()), BorderLayout.CENTER);
        contentPanel.add(offersPanel);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(2, 2));
        buttonsPanel.add(viewButton, BorderLayout.WEST);
        buttonsPanel.add(filterButton, BorderLayout.EAST);
        buttonsPanel.add(showFiltersButton, BorderLayout.WEST);
        buttonsPanel.add(removeFiltersButton, BorderLayout.EAST);
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
        JFrame mainFrame = new JFrame("Buyer: " + username);
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

    private void notifySeen(Offer offer) {
        listener.notifyOfferSeen(offer);
    }

    public void updateOffer(Offer offer) {
        int offerCount = model.getSize();
        if (offer.isClosed()) {
            for (int i = 0; i < offerCount; i++) {
                Offer currentOffer = model.getElementAt(i);
                if (offer.getId() == currentOffer.getId()) {
                    model.remove(i);
                }
            }
        } else {
            boolean exists = false;
            int position = -1;
            for (int i = 0; i < offerCount; i++) {
                Offer currentOffer = model.getElementAt(i);
                if (offer.getId() == currentOffer.getId()) {
                    exists = true;
                    position = i;
                }
            }
            if (exists) {
                model.remove(position);
                model.insertElementAt(offer, position);
            } else {
                model.add(model.getSize(), offer);
            }
        }
    }
}

