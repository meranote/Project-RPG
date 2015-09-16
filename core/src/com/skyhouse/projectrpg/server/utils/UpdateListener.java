package com.skyhouse.projectrpg.server.utils;

import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.skyhouse.projectrpg.ProjectRPGGame;
import com.skyhouse.projectrpg.entities.Character;
import com.skyhouse.projectrpg.entities.data.CharacterData;
import com.skyhouse.projectrpg.server.ProjectRPGServer;

public class UpdateListener {
	
	public static class ClientSide extends Listener {
		
		@Override
		public void received(Connection connection, Object object) {
			if(object instanceof CharactersUpdate) {
				final CharactersUpdate update = (CharactersUpdate)object;
				Gdx.app.postRunnable(new Runnable() {
					@Override
					public void run() {
						if(update.characters == null) return;
						for(Entry<Integer, CharacterData> character : update.characters.entrySet()) {
							int connectionid = character.getKey();
							CharacterData data = character.getValue();
							if(ProjectRPGGame.characters.getOrDefault(connectionid, null) == null) {
								ProjectRPGGame.characters.put(connectionid, new Character(data));
							}
							ProjectRPGGame.characters.get(connectionid).setPosition(data.getPositionX(), data.getPositionY());
							ProjectRPGGame.characters.get(connectionid).setFilpX(data.isFlipX());
							ProjectRPGGame.characters.get(connectionid).setState(data.getState());
						}
					}
				});
			}
		}
	
	}
	
	public static class ServerSide extends Listener {
		
		@Override
		public void received(Connection connection, Object object) {
			if(object instanceof CharacterData) {
				final CharacterData data = (CharacterData)object;
				Gdx.app.postRunnable(new Runnable() {
					@Override
					public void run() {
						ProjectRPGServer.characters.get(data.getID()).getData().setFilpX(data.isFlipX());
						ProjectRPGServer.characters.get(data.getID()).getData().setState(data.getState());
					}
				});
			}
		}
		
	}
	
}
