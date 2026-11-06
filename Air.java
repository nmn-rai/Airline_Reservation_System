import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;

// ================= GUI CLASS =================
public class ReservationGUI extends Frame implements ActionListener {

    TextField nameField, seatField;
    Choice typeChoice;
    Button bookBtn, cancelBtn, cancelSelBtn, displayBtn;
    java.awt.List passengerList;   // clickable list of booked passengers
    TextArea outputArea;
    ReservationSystem system;

    public ReservationGUI() {

        setLayout(new BorderLayout());
        system = new ReservationSystem();

        // ----- FLIGHT PANEL -----
        Panel flightPanel = new Panel(new GridLayout(2, 2));
        flightPanel.setBackground(new Color(220, 230, 250));
        flightPanel.setFont(new Font("Arial", Font.BOLD, 12));
        flightPanel.add(new Label("Flight No: AI-2025"));
        flightPanel.add(new Label("Date: 02-Nov-2025"));
        flightPanel.add(new Label("From: Delhi → Mumbai"));
        flightPanel.add(new Label("Time: 18:30 → 20:45"));

        // ----- INPUT PANEL -----
        Panel inputPanel = new Panel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBackground(new Color(230, 240, 255));

        nameField = new TextField(15);
        seatField = new TextField(5);
        typeChoice = new Choice();
        typeChoice.add("Economy");
        typeChoice.add("Business");

        inputPanel.add(new Label("Passenger Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new Label("Seat No (0–9):"));
        inputPanel.add(seatField);
        inputPanel.add(new Label("Class Type:"));
        inputPanel.add(typeChoice);

        // ----- BUTTON PANEL -----
        Panel buttonPanel = new Panel(new GridLayout(1, 4, 5, 5));
        bookBtn = new Button("Book Seat");
        cancelBtn = new Button("Cancel by Seat No");
        cancelSelBtn = new Button("Cancel Selected");
        displayBtn = new Button("Display List");

        bookBtn.setBackground(new Color(144, 238, 144));
        cancelBtn.setBackground(new Color(255, 182, 193));
        cancelSelBtn.setBackground(new Color(255, 160, 160));
        displayBtn.setBackground(new Color(173, 216, 230));

        buttonPanel.add(bookBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(cancelSelBtn);
        buttonPanel.add(displayBtn);

        // ----- TOP -----
        Panel topPanel = new Panel(new BorderLayout());
        topPanel.add(flightPanel, BorderLayout.NORTH);
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        // ----- PASSENGER LIST (select one to cancel) -----
        passengerList = new java.awt.List(6);
        passengerList.setFont(new Font("Courier New", Font.PLAIN, 12));

        // ----- OUTPUT AREA -----
        outputArea = new TextArea(6, 40);
        outputArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        outputArea.setBackground(new Color(245, 245, 245));
        outputArea.setEditable(false);

        add(topPanel, BorderLayout.NORTH);
        add(passengerList, BorderLayout.CENTER);
        add(outputArea, BorderLayout.SOUTH);

        bookBtn.addActionListener(this);
        cancelBtn.addActionListener(this);
        cancelSelBtn.addActionListener(this);
        displayBtn.addActionListener(this);

        setTitle("Airline Reservation System");
        setSize(650, 520);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setVisible(true);  // MUST BE LAST
    }

    // Rebuild the list box from the system
    private void refreshList() {
        passengerList.removeAll();
        for (Passenger p : system.getBookedPassengers()) {
            passengerList.add(p.getDetails());
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        try {
            if (src == displayBtn) {
                refreshList();
                String all = system.getAllPassengers();
                outputArea.setText(all.isEmpty()
                        ? "No passengers booked yet."
                        : "Passenger List:\n\n" + all);
                return;
            }

            if (src == cancelSelBtn) {
                int idx = passengerList.getSelectedIndex();
                if (idx < 0) {
                    outputArea.setText("Error: Select a passenger from the list first.");
                    return;
                }
                Passenger p = system.getBookedPassengers().get(idx);
                system.cancelPassenger(p.seatNo);
                refreshList();
                outputArea.setText("Cancelled: " + p.name + " (Seat " + p.seatNo + ")");
                return;
            }

            // book / cancel by seat need a seat number
            int seat;
            try {
                seat = Integer.parseInt(seatField.getText().trim());
            } catch (NumberFormatException ex) {
                outputArea.setText("Error: Seat number must be an integer.");
                return;
            }

            if (src == bookBtn) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    outputArea.setText("Error: Enter passenger name.");
                    return;
                }
                Passenger p = typeChoice.getSelectedItem().equals("Economy")
                        ? new EconomyPassenger(name, seat)
                        : new BusinessPassenger(name, seat);
                system.bookPassenger(p);
                outputArea.setText("Booked: " + name + " (Seat " + seat + ")");

            } else if (src == cancelBtn) {
                system.cancelPassenger(seat);
                outputArea.setText("Cancelled booking for Seat " + seat);
            }

            refreshList();

        } catch (Exception ex) {
            outputArea.setText("Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new ReservationGUI();
    }
}

// ================= RESERVATION SYSTEM =================
class ReservationSystem {
    private Passenger[] seats = new Passenger[10];

    public void bookPassenger(Passenger p) throws Exception {
        if (p.seatNo < 0 || p.seatNo >= seats.length)
            throw new Exception("Invalid seat number.");

        if (seats[p.seatNo] != null)
            throw new Exception("Seat already booked.");

        seats[p.seatNo] = p;
    }

    public void cancelPassenger(int seatNo) throws Exception {
        if (seatNo < 0 || seatNo >= seats.length || seats[seatNo] == null)
            throw new Exception("No booking found.");

        seats[seatNo] = null;   // removes only this seat
    }

    // Booked passengers in seat order (same order as the list box)
    public ArrayList<Passenger> getBookedPassengers() {
        ArrayList<Passenger> list = new ArrayList<>();
        for (Passenger p : seats) {
            if (p != null) list.add(p);
        }
        return list;
    }

    public String getAllPassengers() {
        StringBuilder sb = new StringBuilder();
        for (Passenger p : getBookedPassengers()) {
            sb.append(p.getDetails()).append("\n");
        }
        return sb.toString();
    }
}

// ================= PASSENGER CLASSES =================
class Passenger {
    protected String name;
    protected int seatNo;
    protected boolean booked;

    public Passenger(String name, int seatNo) {
        this.name = name;
        this.seatNo = seatNo;
        this.booked = true;
    }

    public String getDetails() {
        return "Seat " + seatNo + ": " + name + " | Status: Booked";
    }
}

class EconomyPassenger extends Passenger {
    public EconomyPassenger(String name, int seatNo) {
        super(name, seatNo);
    }

    @Override
    public String getDetails() {
        return "[Economy] " + super.getDetails();
    }
}

class BusinessPassenger extends Passenger {
    public BusinessPassenger(String name, int seatNo) {
        super(name, seatNo);
    }

    @Override
    public String getDetails() {
        return "[Business] " + super.getDetails();
    }
}
