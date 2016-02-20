package com.ovaube.chroma.networking;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.ovaube.chroma.networking.PacketWorldInfo.BulletInfo;
import com.ovaube.chroma.networking.PacketWorldInfo.PickupInfo;
import com.ovaube.chroma.networking.PacketWorldInfo.PlayerInfo;
import com.ovaube.chroma.util.PlayerColor;

public class ChromaClient extends Listener
{
	private Client client;

	// In
	private PacketServerInfo packetServerInfo;
	private PacketStartGame packetStart;
	private PacketWorldInfo packetWorldInfo;
	// Out
	private PacketClientInfo packetClientInfo = new PacketClientInfo();
	
	private boolean isServerInfoReceived;
	private boolean isServerFull;
	private boolean isFirstPacketReceived;
	private boolean isWorldInfoReceived;
	private boolean isDisconnected;
	private boolean isPacketEndGameReceived;
	
	private String nickname;

	private int portTCP = 54544;
	private int portUDP = 54566;
	
	public ChromaClient() throws Exception
	{
		isServerInfoReceived = false;
		isServerFull = false;
		isFirstPacketReceived = false;
		isWorldInfoReceived = false;
		isDisconnected = false;
		isPacketEndGameReceived = false;
		
		client = new Client();
		// This is a kryonet way to correctly serialize and deserialize data
		registerClasses();
		
		client.start();	
	}
	
	public void connect(InetAddress ip) throws Exception
	{
		client.connect(5000, ip, portTCP, portUDP);
		if(!client.isConnected())
			throw new Exception("Couldn't connect to\n" + ip);
		client.addListener(this);	
	}
	
	private void registerClasses()
	{
		client.getKryo().register(PacketInputInfo.class);
		client.getKryo().register(PacketServerInfo.class);
		client.getKryo().register(PacketClientInfo.class);
		client.getKryo().register(PacketStartGame.class);
		client.getKryo().register(PacketEndGame.class);
		client.getKryo().register(PacketWorldInfo.class);
		client.getKryo().register(PacketDisconnect.class);
		client.getKryo().register(BulletInfo.class);
		client.getKryo().register(PlayerInfo.class);
		client.getKryo().register(PickupInfo.class);
		client.getKryo().register(HashMap.class);
		client.getKryo().register(ArrayList.class);
		client.getKryo().register(PlayerColor.class);
		client.getKryo().register(Vector2.class);
	}
	
	public List<InetAddress> getLocalServers()
	{
		return client.discoverHosts(portUDP, 2500);
	}
	
	public PacketServerInfo getServerInfo()
	{
		return packetServerInfo;
	}
	
	public PacketStartGame getPacketStart()
	{
		return packetStart;
	}
	
	public PacketWorldInfo getPacketWorldInfo()
	{
		return packetWorldInfo;
	}
	
	public void setNickname(String nickname)
	{
		this.nickname = nickname;
	}
	
	public boolean isServerInfoReceived()
	{
		return isServerInfoReceived;
	}
	
	public boolean isServerFull()
	{
		return isServerFull;
	}
	
	public boolean isStartPacketReceived()
	{
		return isFirstPacketReceived;
	}
	
	public boolean isWorldInfoReceived()
	{
		return isWorldInfoReceived;
	}
	
	public boolean isDisconnected()
	{
		return isDisconnected;
	}
	
	public boolean isPacketEndGameReceived()
	{
		return isPacketEndGameReceived;
	}
	
	public Client getKryoClient()
	{
		return client;
	}
	
	@Override
	public void received(Connection c, Object p)
	{
		if(p instanceof PacketServerInfo)
		{
			PacketServerInfo packetServerInfo = (PacketServerInfo)p;
			// If server not full
			if(packetServerInfo.color != null)
			{
				this.packetServerInfo = packetServerInfo;
				isServerInfoReceived = true;
				
				packetClientInfo.nickname = nickname;
				packetClientInfo.color = PlayerColor.valueOf(packetServerInfo.color);
				c.sendTCP(packetClientInfo);
			}
			else
				isServerFull = true;
		}
		
		if(p instanceof PacketStartGame)
		{
			isFirstPacketReceived = true;
			this.packetStart = (PacketStartGame) p;
		}
		
		if(p instanceof PacketWorldInfo)
		{
			isWorldInfoReceived = true;
			this.packetWorldInfo = (PacketWorldInfo) p;
		}
		
		if(p instanceof PacketDisconnect)
		{
			isDisconnected = true;
			client.stop();
			client.close();
		}
		
		if(p instanceof PacketEndGame)
		{
			isPacketEndGameReceived = true;
		}
	}
	
	@Override
	public void disconnected(Connection c)
	{
		isDisconnected = true;
	}
}
