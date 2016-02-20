package com.ovaube.chroma.networking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.ovaube.chroma.networking.PacketWorldInfo.BulletInfo;
import com.ovaube.chroma.networking.PacketWorldInfo.PickupInfo;
import com.ovaube.chroma.networking.PacketWorldInfo.PlayerInfo;
import com.ovaube.chroma.util.PlayerColor;

public class ChromaServer extends Listener
{
	// Kryonet library server
	private Server server;
	
	private int portTCP = 54544;
	private int portUDP = 54566;
	
	// In
	private volatile Array<PacketInputInfo> packets = new Array<PacketInputInfo>();
	
	// Out
	private PacketServerInfo packetServerInfo = new PacketServerInfo();
	
	private Array<PlayerColor> freeColors = new Array<PlayerColor>();
	private HashMap<Integer, PlayerColor> playersConnected = new HashMap<Integer, PlayerColor>();
	private HashMap<PlayerColor, String> playerInfos = new HashMap<PlayerColor, String>();
	private HashMap<PlayerColor, Integer> mapIdsConnected = new HashMap<PlayerColor, Integer>();
	private HashSet<PlayerColor> setPlayersConnected = new HashSet<PlayerColor>();
	
	private String mapName;
	
	public ChromaServer() throws IOException
	{
		init();
	}
	
	private void init() throws IOException
	{
		server = new Server();
		// This is a kryonet way to correctly serialize and deserialize data
		registerClasses();
		
		server.bind(portTCP, portUDP);
		server.start();
		
		server.addListener(this);
	}
	
	private void registerClasses()
	{
		server.getKryo().register(PacketInputInfo.class);
		server.getKryo().register(PacketServerInfo.class);
		server.getKryo().register(PacketClientInfo.class);
		server.getKryo().register(PacketStartGame.class);
		server.getKryo().register(PacketEndGame.class);
		server.getKryo().register(PacketWorldInfo.class);
		server.getKryo().register(PacketDisconnect.class);
		server.getKryo().register(BulletInfo.class);
		server.getKryo().register(PlayerInfo.class);
		server.getKryo().register(PickupInfo.class);
		server.getKryo().register(HashMap.class);
		server.getKryo().register(ArrayList.class);
		server.getKryo().register(PlayerColor.class);
		server.getKryo().register(Vector2.class);
	}
	
	public synchronized Array<PacketInputInfo> getPackets()
	{
		return packets;
	}
	
	public void setFreeColors(Array<PlayerColor> freeColors)
	{
		this.freeColors = freeColors;
	}
	
	public Array<PlayerColor> getFreeColors()
	{
		return freeColors;
	}
	
	public void setMapName(String mapName)
	{
		this.mapName = mapName;
	}
	
	public HashSet<PlayerColor> getPlayersConnected()
	{
		setPlayersConnected.clear();
		
		for(Map.Entry<Integer, PlayerColor> entry : playersConnected.entrySet())
			setPlayersConnected.add(entry.getValue());
		
		return setPlayersConnected;
	}
	
	public HashMap<PlayerColor, Integer> getIdsConnected()
	{
		mapIdsConnected.clear();
		
		for(Map.Entry<Integer, PlayerColor> entry : playersConnected.entrySet())
			mapIdsConnected.put(entry.getValue(), entry.getKey());
		
		return mapIdsConnected;
	}
	
	public HashMap<PlayerColor, String> getPlayerInfos()
	{
		return playerInfos;
	}
	
	public Server getKryoServer()
	{
		return server;
	}
	
	@Override
	public void connected(Connection c)
	{
		if(freeColors.size != 0)
		{
			packetServerInfo.color = freeColors.first().name();
			packetServerInfo.mapName = mapName;
			playersConnected.put(c.getID(), freeColors.first());
			c.sendTCP(packetServerInfo);
			freeColors.removeIndex(0);
		}
		else
		{
			packetServerInfo.color = null;
			c.sendTCP(packetServerInfo);
		}
	}
	
	private synchronized void handlePacketsArray(PacketInputInfo packetArrived)
	{
		for(PacketInputInfo packet : packets)
			if(packet.color.equals(packetArrived.color))
				packets.removeValue(packet, true);
		
		packets.add(packetArrived);
	}
	
	@Override
	public void received(Connection c, Object p)
	{
		// Input info
		if(p instanceof PacketInputInfo)
		{
			PacketInputInfo packetArrived = (PacketInputInfo)p;
			// Replace old with new
			handlePacketsArray(packetArrived);
		}
		// First client info
		if(p instanceof PacketClientInfo)
		{
			PacketClientInfo packetArrived = (PacketClientInfo)p;
			playerInfos.put(packetArrived.color, packetArrived.nickname);
		}
	}
	
	@Override
	public void disconnected(Connection c)
	{
		freeColors.add(playersConnected.get(c.getID()));
		playerInfos.remove(playersConnected.get(c.getID()));
		playersConnected.remove(c.getID());
	}
}
