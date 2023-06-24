Requires Java anf Maven installed

Drawing app:

In the cloned directory run: 
mvn package

To run the server:
java -cp target/pb3-0.0.1-SNAPSHOT-jar-with-dependencies.jar pb.WhiteboardServer

To run each peer in a separate window and a different port number:
java -cp target/pb3-0.0.1-SNAPSHOT-jar-with-dependencies.jar pb.WhiteboardPeer -port 'number'


File sharing app:

In the cloned directory run: 
git checkout project2b_submission 
mvn package
Copy the app the to multiple locations
Put files to be shared in the app location

To run the server:
java -cp target/pb2b-0.0.1-SNAPSHOT-jar-with-dependencies.jar pb.IndexServer

To run peer that shares files:
java -cp target/pb2b-0.0.1-SNAPSHOT-jar-with-dependencies.jar pb.FileSharingPeer -port 'number' -share 'list of filepaths separated by commas'

To run peer that query files:
java -cp target/pb2b-0.0.1-SNAPSHOT-jar-with-dependencies.jar pb.FileSharingPeer -port 'number' -query 'keywords'

To run admin client:
java -cp target/pb2b-0.0.1-SNAPSHOT-jar-with-dependencies.jar pb.AdminClient -host 'IndexServerHost' -port 'IndexServerPort' -action 'shutdown/force/vader'

Optional parameters: -password : password for the index server, can be used by admin user to turn terminate the server remotely
		     -host : for peers, index server host name
		     -indexServerPort : for peers, index server port

Notes: For testing purposes, all shared and downloaded files should be put in the app locations. All file Transfers are slown down for demonstration purposes