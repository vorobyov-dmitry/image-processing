package ua.com.ostrog.photo.ui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import ua.com.ostrog.photo.logic.Controller;
/**
 * The application window. This is the former {@code Extractor} frame and the
 * former {@code ApplicationPanel} merged into a single class: it builds the UI
 * and delegates every file operation to {@link Controller}.
 */
public class Application extends JFrame {
	private static final long serialVersionUID = -423490175756620363L;

	public static final String versionOfProduct = "photo v.3.03";

	private static final String SORT_PHOTOS_TAB_TITLE = "Sort Photos";
	private static final String CONSISTENCY_TAB_TITLE = "Consistency";
	private static final String SYNCHRONIZE_TAB_TITLE = "Synchronize";


	private final JTabbedPane tabbedPane = new JTabbedPane();

	private final SortPanel sortPanel = new SortPanel();
	private final ConsistencyPanel consistencyPanel = new ConsistencyPanel();
	private final SynchronizePanel synchronizePanel = new SynchronizePanel();

	private final Controller controller = new Controller();

	/**
	 * Restores the last-used source/destination directories from user
	 * preferences and builds the UI. Call {@link #showApplication()}
	 * afterwards to display the window.
	 */
	public Application() {
		buildUi();
	}

	private void buildUi() {
		sortPanel.setController(controller);
		tabbedPane.addTab(SORT_PHOTOS_TAB_TITLE, sortPanel);
		tabbedPane.addTab(CONSISTENCY_TAB_TITLE, consistencyPanel);
		tabbedPane.addTab(SYNCHRONIZE_TAB_TITLE, synchronizePanel);
		setContentPane(tabbedPane);
	}




	protected void showApplication() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(1024, 400);
		this.setLocation((screenSize.width - this.getWidth()) / 2,
				(screenSize.height - this.getHeight()) / 2);
		this.setVisible(true);
		this.setTitle(versionOfProduct);
	}

	/**
	 * Application entry point. Creates and shows the main window, and saves
	 * the current source/destination directories to user preferences on close.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		Application application = new Application();
		application.showApplication();
		application.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				application.sortPanel.writeSettings();
				System.exit(0);
			}
		});
	}
}
