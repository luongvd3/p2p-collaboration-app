package pb.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import pb.managers.ClientManager;
import pb.managers.IOThread;
import pb.managers.PeerManager;
import pb.managers.ServerManager;
import pb.managers.endpoint.Endpoint;
import pb.WhiteboardServer;
import pb.utils.Utils;
/**
 * Initial code obtained from:
 * https://www.ssaurel.com/blog/learn-how-to-make-a-swing-painting-and-drawing-application/
 */
public class WhiteboardApp {
	private static Logger log = Logger.getLogger(WhiteboardApp.class.getName());
	
	/**
	 * Emitted to another peer to subscribe to updates for the given board. Argument
	 * must have format "host:port:boardid".
	 * <ul>
	 * <li>{@code args[0] instanceof String}</li>
	 * </ul>
	 */
	public static final String listenBoard = "BOARD_LISTEN";

	/**
	 * Emitted to another peer to unsubscribe to updates for the given board.
	 * Argument must have format "host:port:boardid".
	 * <ul>
	 * <li>{@code args[0] instanceof String}</li>
	 * </ul>
	 */
	public static final String unlistenBoard = "BOARD_UNLISTEN";

	/**
	 * Emitted to another peer to get the entire board data for a given board.
	 * Argument must have format "host:port:boardid".
	 * <ul>
	 * <li>{@code args[0] instanceof String}</li>
	 * </ul>
	 */
	public static final String getBoardData = "GET_BOARD_DATA";

	/**
	 * Emitted to another peer to give the entire board data for a given board.
	 * Argument must have format "host:port:boardid%version%PATHS".
	 * <ul>
	 * <li>{@code args[0] instanceof String}</li>
	 * </ul>
	 */
	public static final String boardData = "BOARD_DATA";

	/**
	 * Emitted to another peer to add a path to a board managed by that peer.
	 * Argument must have format "host:port:boardid%version%PATH". The numeric value
	 * of version must be equal to the version of the board without the PATH added,
	 * i.e. the current version of the board.
	 * <ul>
	 * <li>{@code args[0] instanceof String}</li>
	 * </ul>
	 */
	public static final String boardPathUpdate = "BOARD_PATH_UPDATE";

	/**
	 * Emitted to another peer to indicate a new path has been accepted. Argument
	 * must have format "host:port:boardid%version%PATH". The numeric value of
	 * version must be equal to the version of the board without the PATH added,
	 * i.e. the current version of the board.
	 * <ul>
	 * <li>{@code args[0] instanceof String}</li>
	 * </ul>
	 */
	public static final String boardPathAccepted = "BOARD_PATH_ACCEPTED";

	/**
	 * Emitted to another peer to remove the last path on a board managed by that
	 * peer. Argument must have format "host:port:boardid%version%". The numeric
	 * value of version must be equal to the version of the board without the undo
	 * applied, i.e. the current version of the board.
	 * <ul>
	 * <li>{@code args[0] instanceof String}</li>
	 * </ul>
	 */
	public static final String boardUndoUpdate = "BOARD_UNDO_UPDATE";

	/**
	 * Emitted to another peer to indicate an undo has been accepted. Argument must
	 * have format "host:port:boardid%version%". The numeric value of version must
	 * be equal to the version of the board without the undo applied, i.e. the
	 * current version of the board.
	 * <ul>
	 * <li>{@code args[0] instanceof String}</li>
	 * </ul>
	 */
	public static final String boardUndoAccepted = "BOARD_UNDO_ACCEPTED";

	/**
	 * Emitted to another peer to clear a board managed by that peer. Argument must
	 * have format "host:port:boardid%version%". The numeric value of version must
	 * be equal to the version of the board without the clear applied, i.e. the
	 * current version of the board.
	 * <ul>
	 * <li>{@code args[0] instanceof String}</li>
	 * </ul>
	 */
	public static final String boardClearUpdate = "BOARD_CLEAR_UPDATE";

	/**
	 * Emitted to another peer to indicate an clear has been accepted. Argument must
	 * have format "host:port:boardid%version%". The numeric value of version must
	 * be equal to the version of the board without the clear applied, i.e. the
	 * current version of the board.
	 * <ul>
	 * <li>{@code args[0] instanceof String}</li>
	 * </ul>
	 */
	public static final String boardClearAccepted = "BOARD_CLEAR_ACCEPTED";

	/**
	 * Emitted to another peer to indicate a board no longer exists and should be
	 * deleted. Argument must have format "host:port:boardid".
	 * <ul>
	 * <li>{@code args[0] instanceof String}</li>
	 * </ul>
	 */
	public static final String boardDeleted = "BOARD_DELETED";

	/**
	 * Emitted to another peer to indicate an error has occurred.
	 * <ul>
	 * <li>{@code args[0] instanceof String}</li>
	 * </ul>
	 */
	public static final String boardError = "BOARD_ERROR";

	/**
	 * Emitted locally to indicate the share button has been clicked.
	 * <ul>
	 * <li>{@code args[0] instanceof boolean}</li>
	 * </ul>
	 */
	public static final String shareClicked = "SHARE_CLICKED";

	/**
	 * Emitted locally to indicate a local update to a local board takes place.
	 * <ul>
	 *     <li>{@code args[0] instanceof String}</li>
	 * </ul>
	 */
	public static final String localUpdate = "LOCAL_UPDATE";

	/**
	 * Emitted locally to indicate a local update to a remote board takes place.
	 * <ul>
	 *     <li>{@code args[0] instanceof String}</li>
	 * </ul>
	 */
	public static final String remoteUpdate = "REMOTE_UPDATE";

	/**
	 * Emitted locally to indicate that a remote board is unshared
	 */
	public static final String boardUnshared = "BOARD_UNSHARED";

	/**
	 * White board map from board name to board object 
	 */
	Map<String,Whiteboard> whiteboards;

	/**
	 * Client managers map from board name to clientmanager object
	 */
	Map<String, ClientManager> clientmanagers;
	
	/**
	 * The currently selected white board
	 */
	Whiteboard selectedBoard = null;
	
	/**
	 * The peer:port string of the peer. This is synonomous with IP:port, host:port,
	 * etc. where it may appear in comments.
	 */
	String peerport="standalone"; // a default value for the non-distributed version


	/*
	 * GUI objects, you probably don't need to modify these things... you don't
	 * need to modify these things... don't modify these things [LOTR reference?].
	 */
	
	JButton clearBtn, blackBtn, redBtn, createBoardBtn, deleteBoardBtn, undoBtn;
	JCheckBox sharedCheckbox ;
	DrawArea drawArea;
	JComboBox<String> boardComboBox;
	boolean modifyingComboBox=false;
	boolean modifyingCheckBox=false;

	PeerManager peerManager;
	/**
	 * Initialize the white board app.
	 */
	public WhiteboardApp(int peerPort,String whiteboardServerHost, 
			int whiteboardServerPort) throws UnknownHostException, InterruptedException{
		whiteboards=new HashMap<>();
		peerManager = new PeerManager(peerPort);
		clientmanagers=new HashMap<>();
		/**
		* Main client manager to update and receive updates from the whiteboard server
		*/
		ClientManager mainClientManager;

		mainClientManager = peerManager.connect(whiteboardServerPort,whiteboardServerHost);
		mainClientManager.on(PeerManager.peerStarted, (args) -> {
			Endpoint endpoint = (Endpoint)args[0];
			log.info("Connected to whiteboard server: "+endpoint.getOtherEndpointId());
			peerManager.on(shareClicked,(args1) ->{
				if ((boolean)args1[0]) {
					endpoint.emit(WhiteboardServer.shareBoard,args1[1]);
				} else {
					endpoint.emit(WhiteboardServer.unshareBoard,args1[1]);
				}
			}).on(boardDeleted, (args1) -> {
				log.info("Received board deleted emission");
				String boardName = (String)args1[0];
				Whiteboard whiteboard = whiteboards.get(boardName);
				if(whiteboard!=null) {
					if (!whiteboard.isRemote()) {
						endpoint.emit(WhiteboardServer.unshareBoard, boardName);
						log.info("Unshared board due to deletion: "+boardName);
					} else {
						peerManager.localEmit(boardUnshared, boardName);
						log.info("Shut down board due to deletion: "+boardName);
					}
					whiteboards.remove(boardName);
				}
			});
			endpoint.on(WhiteboardServer.sharingBoard,(args1) -> {
				String newBoardName = (String)args1[0];
				if (!whiteboards.containsKey(newBoardName)) {
					Whiteboard newWhiteboard = new Whiteboard(newBoardName,true);
					newWhiteboard.setShared(true);
					addBoard(newWhiteboard, false);
					// steve
					try{
						queryBoard(newBoardName);
					} catch (Exception e){
						System.out.println(e);
					}
					// steve
				} else {
					log.info("Board is local or already shared");
				}
			}).on(WhiteboardServer.unsharingBoard,(args1) -> {
				String boardToBeRemove = (String)args1[0];
				if (whiteboards.containsKey(boardToBeRemove)) {
					if (whiteboards.get(boardToBeRemove).isRemote()) {
						deleteBoard(boardToBeRemove);
					} else {
						log.info("Can't remove local board");
					}
				} else {
					log.info("Board could not be removed, board doesn't exist");
				}
			});
		}).on(PeerManager.peerError,(args -> {
			Endpoint endpoint = (Endpoint)args[0];
			System.out.println("Disconnected from the whiteboard server with error: "+endpoint.getOtherEndpointId());
		})).on(PeerManager.peerStopped,(args) -> {
			Endpoint endpoint = (Endpoint)args[0];
			System.out.println("Disconnected from the whiteboard server: "+endpoint.getOtherEndpointId());
		});
		mainClientManager.start();
		peerManager.on(PeerManager.peerStarted, (args)->{
			Endpoint endpoint = (Endpoint)args[0];
			System.out.println("Connection from peer: "+endpoint.getOtherEndpointId());
			// steve
			endpoint.on(getBoardData, (args1) -> {
				String boardName = (String)args1[0];
				// if the board is a local board and is being shared, emit the board data
				if(!whiteboards.get(boardName).isRemote()&&whiteboards.get(boardName).isShared()){
					endpoint.emit(boardData, whiteboards.get(boardName).toString());
				}
			}).on(listenBoard, (args1) -> {
				String boardName = (String)args1[0]; // this boardName is just the board name
				peerManager.on(localUpdate, (args2) -> {
					String eventName = (String)args2[0];
					if(!whiteboards.get(boardName).isRemote()&&whiteboards.get(boardName).isShared()){
						endpoint.emit(eventName, whiteboards.get(boardName).toString());
					}else{
						endpoint.emit(boardError, "The board is not local or not shared! Wrong event emitted!");
					}
				});

			}).on(boardPathUpdate, (args1) -> {
				String board = (String)args1[0]; // complete board data
				if(whiteboards.containsKey(getBoardName(board))&&getBoardVersion(board)-1==whiteboards.get(getBoardName(board)).getVersion()){
					// add the path locally and emit to other endpoints listening as well
					String[] paths = getBoardPaths(board).split("%");
					whiteboards.get(getBoardName(board)).addPath(new WhiteboardPath(paths[paths.length-1]), whiteboards.get(getBoardName(board)).getVersion());
					drawSelectedWhiteboard();
					peerManager.localEmit(localUpdate, boardPathUpdate);
					endpoint.emit(boardPathAccepted, board);
				}else{
					endpoint.emit(boardError, "Something went wrong...");
				}
			}).on(boardClearUpdate, (args1) -> {
				String board = (String)args1[0]; // complete board data
				if(whiteboards.containsKey(getBoardName(board))&&getBoardVersion(board)-1==whiteboards.get(getBoardName(board)).getVersion()){
					// clear the board locally and emit to other endpoints listening as well
					whiteboards.get(getBoardName(board)).clear(whiteboards.get(getBoardName(board)).getVersion());
					drawSelectedWhiteboard();
					peerManager.localEmit(localUpdate, boardClearUpdate);
					endpoint.emit(boardClearAccepted, board);
				}else{
					endpoint.emit(boardError, "Something went wrong...");
				}
			}).on(boardUndoUpdate, (args1) -> {
				String board = (String)args1[0]; // complete board data
				if(whiteboards.containsKey(getBoardName(board))&&getBoardVersion(board)-1==whiteboards.get(getBoardName(board)).getVersion()){
					// undo the board locally and emit to other endpoints listening as well
					whiteboards.get(getBoardName(board)).undo(whiteboards.get(getBoardName(board)).getVersion());
					drawSelectedWhiteboard();
					peerManager.localEmit(localUpdate, boardUndoUpdate);
					endpoint.emit(boardUndoAccepted, board);
				}else{
					endpoint.emit(boardError, "Something went wrong...");
				}
			});
		}).on(PeerManager.peerStopped,(args)->{
			Endpoint endpoint = (Endpoint)args[0];
			System.out.println("Disconnected from peer: "+endpoint.getOtherEndpointId());
		}).on(PeerManager.peerError,(args)->{
			Endpoint endpoint = (Endpoint)args[0];
			System.out.println("There was an error communicating with the peer: "
					+endpoint.getOtherEndpointId());
		}).on(PeerManager.peerServerManager, (args)->{
			ServerManager serverManager = (ServerManager)args[0];
			serverManager.on(IOThread.ioThread, (args2)->{
				peerport = (String) args2[0];
				show(peerport);
			});
		}).on(boardUnshared, (args) -> {
			String boardToRemove = (String)args[0];
			System.out.println("Removing board");
			clientmanagers.get(boardToRemove).shutdown();
			clientmanagers.remove(boardToRemove);
			System.out.println("Board removed");
		});
		peerManager.start();
		mainClientManager.join();
	}
	
	/******
	 * 
	 * Utility methods to extract fields from argument strings.
	 * 
	 ******/
	
	/**
	 * 
	 * @param data = peer:port:boardid%version%PATHS
	 * @return peer:port:boardid
	 */
	public static String getBoardName(String data) {
		String[] parts=data.split("%",2);
		return parts[0];
	}
	
	/**
	 * 
	 * @param data = peer:port:boardid%version%PATHS
	 * @return boardid%version%PATHS
	 */
	public static String getBoardIdAndData(String data) {
		String[] parts=data.split(":");
		return parts[2];
	}
	
	/**
	 * 
	 * @param data = peer:port:boardid%version%PATHS
	 * @return version%PATHS
	 */
	public static String getBoardData(String data) {
		String[] parts=data.split("%",2);
		return parts[1];
	}
	
	/**
	 * 
	 * @param data = peer:port:boardid%version%PATHS
	 * @return version
	 */
	public static long getBoardVersion(String data) {
		String[] parts=data.split("%",3);
		return Long.parseLong(parts[1]);
	}
	
	/**
	 * 
	 * @param data = peer:port:boardid%version%PATHS
	 * @return PATHS
	 */
	public static String getBoardPaths(String data) {
		String[] parts=data.split("%",3);
		return parts[2];
	}
	
	/**
	 * 
	 * @param data = peer:port:boardid%version%PATHS
	 * @return peer
	 */
	public static String getIP(String data) {
		String[] parts=data.split(":");
		return parts[0];
	}
	
	/**
	 * 
	 * @param data = peer:port:boardid%version%PATHS
	 * @return port
	 */
	public static int getPort(String data) {
		String[] parts=data.split(":");
		return Integer.parseInt(parts[1]);
	}
	
	/******
	 * 
	 * Methods called from events.
	 * 
	 ******/
	
	// From whiteboard server
	
	
	// From whiteboard peer

	/***
	 * This function is used to respond to or with events relating to querying board
	 * @param boardName
	 * @throws InterruptedException
	 * @throws UnknownHostException
	 */
	private void queryBoard(String boardName) throws InterruptedException, UnknownHostException {
		ClientManager peer = peerManager.connect(getPort(boardName), getIP(boardName));
		peer.on(PeerManager.peerStarted, (args) -> {
			Endpoint endpoint = (Endpoint)args[0];
			System.out.println("Connected to peer: " + endpoint.getOtherEndpointId());
			// waiting for remote board data
			endpoint.on(boardData, (args1) -> {
				String board = (String)args1[0]; // this board is a complete string containing version, paths, etc.
				Whiteboard wb = whiteboards.get(getBoardName(board));
				wb.whiteboardFromString(getBoardName(board), getBoardData(board));
				whiteboards.put(getBoardName(board), wb);
				peerManager.on(remoteUpdate, (args2) -> {
					if(endpoint.isAlive()) {
						String eventName = (String) args2[0];
						if (whiteboards.get(getBoardName(board)).isRemote() && whiteboards.get(getBoardName(board)).isShared()) {
							endpoint.emit(eventName, whiteboards.get(getBoardName(board)).toString());
						} else {
							endpoint.emit(boardError, "The board is not remote or not shared! Wrong event emitted!");
						}
					}
				});
				// after receiving remote board data, ask to listen to updates
				endpoint.emit(listenBoard, getBoardName(board));
			}).on(boardPathUpdate, (args1) -> {
				String board = (String)args1[0]; // complete board data
				if(whiteboards.containsKey(getBoardName(board))&&getBoardVersion(board)-1==whiteboards.get(getBoardName(board)).getVersion()){
					// add the new path to the current board
					String[] paths = getBoardPaths(board).split("%");
					whiteboards.get(getBoardName(board)).addPath(new WhiteboardPath(paths[paths.length-1]), whiteboards.get(getBoardName(board)).getVersion());
					drawSelectedWhiteboard();
				}else{
					endpoint.emit(boardError, "Something went wrong...");
				}
			}).on(boardClearUpdate, (args1) -> {
				String board = (String)args1[0]; // complete board data
				if(whiteboards.containsKey(getBoardName(board))&&getBoardVersion(board)-1==whiteboards.get(getBoardName(board)).getVersion()){
					// clear the board
					whiteboards.get(getBoardName(board)).clear(whiteboards.get(getBoardName(board)).getVersion());
					drawSelectedWhiteboard();
				}else{
					endpoint.emit(boardError, "Something went wrong...");
				}
			}).on(boardUndoUpdate, (args1) -> {
				String board = (String)args1[0]; // complete board data
				if(whiteboards.containsKey(getBoardName(board))&&getBoardVersion(board)-1==whiteboards.get(getBoardName(board)).getVersion()){
					// clear the board
					whiteboards.get(getBoardName(board)).undo(whiteboards.get(getBoardName(board)).getVersion());
					drawSelectedWhiteboard();
				}else{
					endpoint.emit(boardError, "Something went wrong...");
				}
			}).on(boardPathAccepted, (args1) -> {
				drawSelectedWhiteboard();
			}).on(boardClearAccepted, (args1) -> {
				drawSelectedWhiteboard();
			}).on(boardUndoAccepted, (args1) -> {
				drawSelectedWhiteboard();
			});
			// emit a getBoardData to ask for remote board data
			endpoint.emit(getBoardData, boardName);
		}).on(PeerManager.peerStopped, (args)->{
			Endpoint endpoint = (Endpoint)args[0];
			System.out.println("Disconnected from the peer: "+endpoint.getOtherEndpointId());
		}).on(PeerManager.peerError, (args)->{
			Endpoint endpoint = (Endpoint)args[0];
			System.out.println("There was an error communicating with the peer: "
					+endpoint.getOtherEndpointId());
		});
		clientmanagers.put(boardName, peer);
		peer.start();
	}
	
	/******
	 * 
	 * Methods to manipulate data locally. Distributed systems related code has been
	 * cut from these methods.
	 * 
	 ******/
	
	/**
	 * Wait for the peer manager to finish all threads.
	 */
	public void waitToFinish() {
		peerManager.joinWithClientManagers();
	}
	
	/**
	 * Add a board to the list that the user can select from. If select is
	 * true then also select this board.
	 * @param whiteboard
	 * @param select
	 */
	public void addBoard(Whiteboard whiteboard,boolean select) {
		synchronized(whiteboards) {
			whiteboards.put(whiteboard.getName(), whiteboard);
		}
		updateComboBox(select?whiteboard.getName():null);
	}
	
	/**
	 * Delete a board from the list.
	 * @param boardname must have the form peer:port:boardid
	 */
	public void deleteBoard(String boardname) {
		synchronized(whiteboards) {
			Whiteboard whiteboard = whiteboards.get(boardname);
			if(whiteboard!=null) {
				//whiteboards.remove(boardname);
				peerManager.localEmit(boardDeleted, boardname);
				log.info("Board being deleted: "+boardname);
			}
		}
		updateComboBox(null);
	}
	
	/**
	 * Create a new local board with name peer:port:boardid.
	 * The boardid includes the time stamp that the board was created at.
	 */
	public void createBoard() {
		String name = peerport+":board"+Instant.now().toEpochMilli();
		Whiteboard whiteboard = new Whiteboard(name,false);
		addBoard(whiteboard,true);
	}
	
	/**
	 * Add a path to the selected board. The path has already
	 * been drawn on the draw area; so if it can't be accepted then
	 * the board needs to be redrawn without it.
	 * @param currentPath
	 */
	public void pathCreatedLocally(WhiteboardPath currentPath) {
		if(selectedBoard!=null) {
			if(!selectedBoard.addPath(currentPath,selectedBoard.getVersion())) {
				// some other peer modified the board in between
				drawSelectedWhiteboard(); // just redraw the screen without the path
			} else {
				// was accepted locally, so do remote stuff if needed
				if(!selectedBoard.isRemote()) {
					peerManager.localEmit(localUpdate, boardPathUpdate);
				}else{
					peerManager.localEmit(remoteUpdate, boardPathUpdate);
				}
			}
		} else {
			log.severe("path created without a selected board: "+currentPath);
		}
	}
	
	/**
	 * Clear the selected whiteboard.
	 */
	public void clearedLocally() {
		if(selectedBoard!=null) {
			if(!selectedBoard.clear(selectedBoard.getVersion())) {
				// some other peer modified the board in between
				drawSelectedWhiteboard();
			} else {
				// was accepted locally, so do remote stuff if needed
				drawSelectedWhiteboard();
				if(!selectedBoard.isRemote()){
					peerManager.localEmit(localUpdate, boardClearUpdate);
				}else{
					peerManager.localEmit(remoteUpdate, boardClearUpdate);
				}
			}
		} else {
			log.severe("cleared without a selected board");
		}
	}
	
	/**
	 * Undo the last path of the selected whiteboard.
	 */
	public void undoLocally() {
		if(selectedBoard!=null) {
			if(!selectedBoard.undo(selectedBoard.getVersion())) {
				// some other peer modified the board in between
				drawSelectedWhiteboard();
			} else {
				drawSelectedWhiteboard();
				if(!selectedBoard.isRemote()){
					peerManager.localEmit(localUpdate, boardUndoUpdate);
				}else{
					peerManager.localEmit(remoteUpdate, boardUndoUpdate);
				}
			}
		} else {
			log.severe("undo without a selected board");
		}
	}
	
	/**
	 * The variable selectedBoard has been set.
	 */
	public void selectedABoard() {
		drawSelectedWhiteboard();
		log.info("selected board: "+selectedBoard.getName());
	}
	
	/**
	 * Set the share status on the selected board.
	 */
	public void setShare(boolean share) {
		if(selectedBoard!=null) {
        	selectedBoard.setShared(share);
			if (!selectedBoard.isRemote()) {
				peerManager.localEmit(shareClicked,share,selectedBoard.getName());
			}
        } else {
        	log.severe("there is no selected board");
        }
	}
	
	/**
	 * Called by the gui when the user closes the app.
	 */
	public void guiShutdown() {
		// do some final cleanup
		HashSet<Whiteboard> existingBoards= new HashSet<>(whiteboards.values());
		existingBoards.forEach((board)->{
			deleteBoard(board.getName());
		});
    	whiteboards.values().forEach((whiteboard)->{
    		
    	});
    	peerManager.shutdown();
	}
	
	

	/******
	 * 
	 * GUI methods and callbacks from GUI for user actions.
	 * You probably do not need to modify anything below here.
	 * 
	 ******/
	
	/**
	 * Redraw the screen with the selected board
	 */
	public void drawSelectedWhiteboard() {
		drawArea.clear();
		if(selectedBoard!=null) {
			selectedBoard.draw(drawArea);
		}
	}
	
	/**
	 * Setup the Swing components and start the Swing thread, given the
	 * peer's specific information, i.e. peer:port string.
	 */
	public void show(String peerport) {
		// create main frame
		JFrame frame = new JFrame("Whiteboard Peer: "+peerport);
		Container content = frame.getContentPane();
		// set layout on content pane
		content.setLayout(new BorderLayout());
		// create draw area
		drawArea = new DrawArea(this);

		// add to content pane
		content.add(drawArea, BorderLayout.CENTER);

		// create controls to apply colors and call clear feature
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

		/**
		 * Action listener is called by the GUI thread.
		 */
		ActionListener actionListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == clearBtn) {
					clearedLocally();
				} else if (e.getSource() == blackBtn) {
					drawArea.setColor(Color.black);
				} else if (e.getSource() == redBtn) {
					drawArea.setColor(Color.red);
				} else if (e.getSource() == boardComboBox) {
					if(modifyingComboBox) return;
					if(boardComboBox.getSelectedIndex()==-1) return;
					String selectedBoardName=(String) boardComboBox.getSelectedItem();
					if(whiteboards.get(selectedBoardName)==null) {
						log.severe("selected a board that does not exist: "+selectedBoardName);
						return;
					}
					selectedBoard = whiteboards.get(selectedBoardName);
					// remote boards can't have their shared status modified
					if(selectedBoard.isRemote()) {
						sharedCheckbox.setEnabled(false);
						sharedCheckbox.setVisible(false);
					} else {
						modifyingCheckBox=true;
						sharedCheckbox.setSelected(selectedBoard.isShared());
						modifyingCheckBox=false;
						sharedCheckbox.setEnabled(true);
						sharedCheckbox.setVisible(true);
					}
					selectedABoard();
				} else if (e.getSource() == createBoardBtn) {
					createBoard();
				} else if (e.getSource() == undoBtn) {
					if(selectedBoard==null) {
						log.severe("there is no selected board to undo");
						return;
					}
					undoLocally();
				} else if (e.getSource() == deleteBoardBtn) {
					if(selectedBoard==null) {
						log.severe("there is no selected board to delete");
						return;
					}
					deleteBoard(selectedBoard.getName());
				}
			}
		};
		
		clearBtn = new JButton("Clear Board");
		clearBtn.addActionListener(actionListener);
		clearBtn.setToolTipText("Clear the current board - clears remote copies as well");
		clearBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		blackBtn = new JButton("Black");
		blackBtn.addActionListener(actionListener);
		blackBtn.setToolTipText("Draw with black pen");
		blackBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		redBtn = new JButton("Red");
		redBtn.addActionListener(actionListener);
		redBtn.setToolTipText("Draw with red pen");
		redBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		deleteBoardBtn = new JButton("Delete Board");
		deleteBoardBtn.addActionListener(actionListener);
		deleteBoardBtn.setToolTipText("Delete the current board - only deletes the board locally");
		deleteBoardBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		createBoardBtn = new JButton("New Board");
		createBoardBtn.addActionListener(actionListener);
		createBoardBtn.setToolTipText("Create a new board - creates it locally and not shared by default");
		createBoardBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		undoBtn = new JButton("Undo");
		undoBtn.addActionListener(actionListener);
		undoBtn.setToolTipText("Remove the last path drawn on the board - triggers an undo on remote copies as well");
		undoBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		sharedCheckbox = new JCheckBox("Shared");
		sharedCheckbox.addItemListener(new ItemListener() {    
	         public void itemStateChanged(ItemEvent e) { 
	            if(!modifyingCheckBox) setShare(e.getStateChange()==1);
	         }    
	      }); 
		sharedCheckbox.setToolTipText("Toggle whether the board is shared or not - tells the whiteboard server");
		sharedCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
		

		// create a drop list for boards to select from
		JPanel controlsNorth = new JPanel();
		boardComboBox = new JComboBox<String>();
		boardComboBox.addActionListener(actionListener);
		
		
		// add to panel
		controlsNorth.add(boardComboBox);
		controls.add(sharedCheckbox);
		controls.add(createBoardBtn);
		controls.add(deleteBoardBtn);
		controls.add(blackBtn);
		controls.add(redBtn);
		controls.add(undoBtn);
		controls.add(clearBtn);

		// add to content pane
		content.add(controls, BorderLayout.WEST);
		content.add(controlsNorth,BorderLayout.NORTH);

		frame.setSize(600, 600);
		
		// create an initial board
		createBoard();
		
		// closing the application
		frame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent windowEvent) {
		        if (JOptionPane.showConfirmDialog(frame, 
		            "Are you sure you want to close this window?", "Close Window?", 
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
		        {
		        	guiShutdown();
		            frame.dispose();
		        }
		    }
		});
		
		// show the swing paint result
		frame.setVisible(true);
		
	}
	
	/**
	 * Update the GUI's list of boards. Note that this method needs to update data
	 * that the GUI is using, which should only be done on the GUI's thread, which
	 * is why invoke later is used.
	 * 
	 * @param select, board to select when list is modified or null for default
	 *                selection
	 */
	private void updateComboBox(String select) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if(boardComboBox == null){
					return;
				}
				modifyingComboBox=true;
				boardComboBox.removeAllItems();
				int anIndex=-1;
				synchronized(whiteboards) {
					ArrayList<String> boards = new ArrayList<String>(whiteboards.keySet());
					Collections.sort(boards);
					for(int i=0;i<boards.size();i++) {
						String boardname=boards.get(i);
						boardComboBox.addItem(boardname);
						if(select!=null && select.equals(boardname)) {
							anIndex=i;
						} else if(anIndex==-1 && selectedBoard!=null && 
								selectedBoard.getName().equals(boardname)) {
							anIndex=i;
						} 
					}
				}
				modifyingComboBox=false;
				if(anIndex!=-1) {
					boardComboBox.setSelectedIndex(anIndex);
				} else {
					if(whiteboards.size()>0) {
						boardComboBox.setSelectedIndex(0);
					} else {
						drawArea.clear();
						createBoard();
					}
				}
				
			}
		});
	}
	
}
