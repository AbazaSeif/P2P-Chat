/*
 * P2PChat - Peer-to-Peer Chat Application
 *
 * Copyright (c) 2014 Ahmed Samy  <f.fallen45@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package centralpoint;

import netlib.AsyncCallbacks;
import netlib.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import java.util.List;

/*
 * This class simulates a peer-to-peer hybrid model.
 * However, this is not all of the model, this is just
 * the Central Point for which the peers would communicate
 * with to retrieve a list of available peers.
 *
 * Few notes on the bytes used:
 *  Once a peer has connected to this server, it must send:
 *	0x1A to retrieve the peer list.
 *  The peer list is sent as follows:
 *	Integer - Number of available peers
 *	byte[4] for each peer address
 *
 * So for example:
 *  3
 *  127.0.0.1
 *  192.168.1.1
 *  138.158.15.69
*/
public class HybridCentralPoint implements AsyncCallbacks {
    private Server m_server;
    private List<InetAddress> m_peerAddresses = new ArrayList<InetAddress>();

	public HybridCentralPoint() throws IOException {
		m_server = new Server(null, 9118, this);
		new Thread(m_server).start();
	}

	@Override
	public boolean handleWrite(SocketChannel ch, int nr_wrote) {
		return true;
	}

	@Override
	public boolean handleRead(SocketChannel ch, ByteBuffer buf, int nread) {
		byte[] data = buf.array();

		if (data[0] == 0x1A) {
			ByteBuffer buffer = ByteBuffer.allocate(1024);

			buffer.putInt(m_peerAddresses.size() - 1);
			for (InetAddress address : m_peerAddresses)
				if (address != ch.socket().getInetAddress())
					buffer.put(address.getAddress());

			m_server.send(ch, buffer.array());
			m_server.close(ch);
			return true;
		}

		return false;
    }

    @Override
    public boolean handleConnection(SocketChannel ch) {
		m_peerAddresses.add(ch.socket().getInetAddress());
		return true;
    }

    public static void main(String args[]) {
		try {
			new HybridCentralPoint();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
