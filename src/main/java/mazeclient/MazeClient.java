package mazeclient;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import mazeclient.generated.LoginMessageType;
import mazeclient.generated.MazeCom;
import mazeclient.generated.ObjectFactory;

/**
 * Created by mbenndorf on 20.06.2017.
 */
public class MazeClient {

	private Socket socket;
	private UTFInputStream inputStream;
	private UTFOutputStream outputStream;
	private ObjectFactory objectFactory;
	private JAXBContext jaxbContext;
	private Marshaller marshaller;
	private Unmarshaller unmarshaller;

	public MazeClient() {
		objectFactory = new ObjectFactory();
		try {
			jaxbContext = JAXBContext.newInstance(MazeCom.class);
			marshaller = jaxbContext.createMarshaller();
			unmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	public boolean connect(String hostname, int port) {
		try {
			socket = new Socket(hostname, port);
			inputStream = new UTFInputStream(socket.getInputStream());
			outputStream = new UTFOutputStream(socket.getOutputStream());
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public boolean handshake() {
		// send login msg
		MazeCom mazeCom = null;// TODO
		LoginMessageType loginMessageType = null;// TODO
		loginMessageType.setName("Martin");
		mazeCom.setLoginMessage(loginMessageType);
		send(mazeCom);
		// read login reply msg
		// TODO handshake
		return false;
	}

	private void send(MazeCom msg) {
		try {
			String xml = messageToXMLString(msg);
			outputStream.writeUTF8(xml);
		} catch (IOException | JAXBException e) {
			e.printStackTrace();
		}
	}

	private MazeCom read() {
		MazeCom msg = null;
		try {
			String xml = inputStream.readUTF8();
			msg = xmlStringToMessage(xml);
		} catch (IOException | JAXBException e) {
			e.printStackTrace();
		}
		return msg;
	}

	MazeCom xmlStringToMessage(String xml) throws JAXBException {
		StringReader sr = new StringReader(xml);
		return (MazeCom) unmarshaller.unmarshal(sr);
	}

	String messageToXMLString(MazeCom message) throws JAXBException {
		StringWriter sw = new StringWriter();
		marshaller.marshal(message, sw);
		System.out.println(sw.toString());
		return sw.toString();
	}
}
