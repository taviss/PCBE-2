package com.pcbe.stock.buyer.ui;

import com.pcbe.stock.buyer.StockConsumer;
import com.pcbe.stock.event.Offer;
import com.pcbe.stock.ui.OfferView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.*;

public class StockConsumerGUI {
    private static final Logger LOG = LoggerFactory.getLogger(StockConsumerGUI.class);

    private StockConsumer listener;
    private JList<Offer> offerList = new JList<Offer>();
    private DefaultListModel<Offer> model;
    private String username;
    private ArrayList<String> filters = new ArrayList<String>();
    private float minPrice = 0;
    private float maxPrice = 999999999;
    private String company;
    private DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    private Date date;
    private float price;
    
    private ImageIcon icon;

    public StockConsumerGUI(String username, float minPrice, float maxPrice) {
        try {
            date = format.parse("1/1/1000");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.username=username;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
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
                    bidFrame.setLocationRelativeTo(null);
                    if(icon != null) {
                        bidFrame.setIconImage(icon.getImage());
                    }
                    JPanel offerPanel = new JPanel();
                    offerPanel.setLayout(new BoxLayout(offerPanel, BoxLayout.Y_AXIS));
                    offerPanel.add(new JLabel("Description: " + selectedOffer.getDescription()));
                    JPanel inputPrice = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    inputPrice.add(new JLabel("Price:"));
                    JTextField priceField = new JTextField(10);
                    priceField.setText("");
                    inputPrice.add(priceField);
                    offerPanel.add(inputPrice);
                    JButton bidButton = new JButton("Bid");
                    JButton cancelButton = new JButton("Cancel");
                    bidButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            priceField.selectAll();
                            try {
                                price = Float.parseFloat(priceField.getText());
                                if(price<selectedOffer.getPrice()) {
                                    JOptionPane.showMessageDialog(null, "Your offer was below the current price.");
                                } else {
                                    selectedOffer.setPrice(price);
                                    selectedOffer.setHighestBidder(username);
                                    notifyBid(selectedOffer);
                                }
                                bidFrame.dispatchEvent(new WindowEvent(bidFrame, WindowEvent.WINDOW_CLOSING));
                            }catch(NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, "The price value is not a number.");
                            }
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
                        new String[] { "Company", "MinPrice", "MaxPrice", "MinDate" });
                input.setText(company);
                comboBox.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if(e.getStateChange() == ItemEvent.SELECTED) {
                            switch (e.getItem().toString()) {
                                case "Company": {
                                    input.setText(company);
                                    break;
                                }
                                case "MinPrice": {
                                    input.setText(String.valueOf(minPrice));
                                    break;
                                }
                                case "MaxPrice": {
                                    input.setText(String.valueOf(maxPrice));
                                    break;
                                }
                                case "MinDate": {
                                    input.setText(format.format(date));
                                    break;
                                }
                            }
                        }
                    }
                });
                selectPanel.add(input);
                selectPanel.add(comboBox);
                int result = JOptionPane.showConfirmDialog(null, selectPanel, "Filter offers:",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                switch (result) {
                    case JOptionPane.OK_OPTION:
                        if(comboBox.getSelectedItem().equals("Company")) {
                            company = input.getText();
                        }
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
                        if(comboBox.getSelectedItem().equals("MinDate")) {
                            String string = input.getText();
                            try {
                                date = format.parse(string);
                            } catch (ParseException e1) {
                                JOptionPane.showMessageDialog(null, "Date format should be: dd/MM/yyyy");
                            }
                        }
                        //notifySubscribeTo(comboBox.getSelectedItem() + ":" + input.getText());
                        listener.setMinPrice(minPrice);
                        listener.setMaxPrice(maxPrice);
                        listener.setCompany(company);
                        listener.setMinDate(date.getTime());
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
        JButton refilterButton = new JButton("Apply filters");
        refilterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(listener.resubscribe()) {
                    model.clear();
                }
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
        buttonsPanel.add(refilterButton, BorderLayout.EAST);
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
        URL img = getClass().getClassLoader().getResource("buyer.png");
        if(img != null) {
            icon = new ImageIcon(img);
            mainFrame.setIconImage(icon.getImage());
        }
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel contentPanel = initContent();
        mainPanel.add(contentPanel, BorderLayout.WEST);
        mainFrame.add(mainPanel);
        mainFrame.setResizable(true);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
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
    
    private void notifyBid(Offer offer) {
        listener.notifyOfferBid(offer);
    }

    public void updateOffer(Offer offer) {
        LOG.info("Updating offer " + offer.getCompany() + ":" + offer.getId());
        if(model.contains(offer))
            return;
        
        int offerCount = model.getSize();
        if (offer.isClosed()) {
            LOG.info("Closed offer " + offer.getCompany() + ":" + offer.getId());
            for (int i = 0; i < offerCount; i++) {
                Offer currentOffer = model.getElementAt(i);
                if (offer.getId() == currentOffer.getId()) {
                    model.remove(i);
                    break;
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
                    
                    //Remove the offer if the new price doesn't match criteria
                    if(offer.getPrice() < minPrice || offer.getPrice() > maxPrice) {
                        model.remove(i);
                        LOG.info("Removed offer " + offer.getCompany() + ":" + offer.getId());
                        return;
                    }
                }
            }
            if (exists) {
                LOG.info("Updated offer " + offer.getCompany() + ":" + offer.getId());
                model.remove(position);
                model.insertElementAt(offer, position);
            } else {
                LOG.info("Added offer " + offer.getCompany() + ":" + offer.getId());
                model.add(model.getSize(), offer);
            }
        }
    }
}

