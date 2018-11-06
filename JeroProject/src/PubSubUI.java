import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

public class PubSubUI extends JFrame {

	private JPanel contentPane;
	private JTextField txtTopic;
	private JTextField txtHost;
	private JTextField txtPort;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PubSubUI frame = new PubSubUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public PubSubUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 667, 574);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		txtTopic = new JTextField();
		txtTopic.setBounds(72, 24, 207, 20);
		contentPane.add(txtTopic);
		txtTopic.setColumns(10);
		
		JLabel lblTopic = new JLabel("Topic:");
		lblTopic.setBounds(26, 27, 46, 14);
		contentPane.add(lblTopic);
		
		JLabel lblHost = new JLabel("Host:");
		lblHost.setBounds(26, 52, 46, 14);
		contentPane.add(lblHost);
		
		txtHost = new JTextField();
		txtHost.setBounds(72, 52, 207, 20);
		contentPane.add(txtHost);
		txtHost.setColumns(10);
		
		JLabel lblPort = new JLabel("Port:");
		lblPort.setBounds(26, 90, 46, 14);
		contentPane.add(lblPort);
		
		txtPort = new JTextField();
		txtPort.setBounds(72, 87, 207, 20);
		contentPane.add(txtPort);
		txtPort.setColumns(10);
		
		JRadioButton rdbtnPub = new JRadioButton("Publisher");
		rdbtnPub.setBounds(427, 37, 109, 23);
		contentPane.add(rdbtnPub);
		
		JRadioButton rdbtnSub = new JRadioButton("Subscriber");
		rdbtnSub.setBounds(427, 74, 109, 23);
		contentPane.add(rdbtnSub);
		
		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnPub);
		group.add(rdbtnSub);
		
		JTextArea textAreaMessage = new JTextArea();
		textAreaMessage.setWrapStyleWord(true);
		textAreaMessage.setLineWrap(true);
		textAreaMessage.setBounds(67, 137, 512, 156);
		contentPane.add(textAreaMessage);
		
		JLabel lblMessage = new JLabel("Message:");
		lblMessage.setBounds(11, 142, 46, 14);
		contentPane.add(lblMessage);
		
		JTextArea textAreaResult = new JTextArea();
		textAreaResult.setBounds(67, 336, 512, 156);
		contentPane.add(textAreaResult);
		
		JLabel lblResult = new JLabel("Result:");
		lblResult.setBounds(26, 341, 46, 14);
		contentPane.add(lblResult);
	}
}
