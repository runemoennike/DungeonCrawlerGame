/**
 * The message that is passed via MTMessageQueue.
 * Defines the message type as well as any payload.
 */
package threadMessaging;

public class MTMessage {
	public enum MessageType {
		TEST, CURPOS, PATHLIST, STATE, FINDPATH, PATHMAP, ENTID, FOOTPRINT, MAPLEVEL, KILL, TARGETID, SEARCHEDPATH, SENDSTAT, AISTAT
	};

	public MTMessage.MessageType type;
	public Object obj;

	public MTMessage(MTMessage.MessageType type, Object obj) {
		this.type = type;
		this.obj = obj;
	}

	@Override
	public String toString() {
		return "MTMessage(" + type + ", " + obj.toString() + ")";
	}
}