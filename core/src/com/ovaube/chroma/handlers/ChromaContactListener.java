package com.ovaube.chroma.handlers;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.ovaube.chroma.objects.Bullet;
import com.ovaube.chroma.objects.Damager;
import com.ovaube.chroma.objects.Healer;
import com.ovaube.chroma.objects.Pickup;
import com.ovaube.chroma.objects.Player;
import com.ovaube.chroma.util.Constants;
import com.ovaube.chroma.util.PickupType;

public class ChromaContactListener implements ContactListener
{	
	@Override
	public void beginContact(Contact contact) 
	{
		Fixture fixtureA = contact.getFixtureA(); 
		Fixture fixtureB = contact.getFixtureB(); 
		
		// Happens from time to time
		if(fixtureA == null || fixtureB == null) return;
		
		// Bullet <-> ...
		if(fixtureA.getUserData().getClass() == Bullet.class)
			bulletHit(fixtureA, fixtureB);		
		if(fixtureB.getUserData().getClass() == Bullet.class)
			bulletHit(fixtureB, fixtureA);	
		
		// Healer <-> Player
		if(fixtureA.getUserData().getClass() == Healer.class &&
			fixtureB.getUserData().getClass() == Player.class)
		{
			begin_healer_player(fixtureA, fixtureB);
		}
		if(fixtureB.getUserData().getClass() == Healer.class &&
			fixtureA.getUserData().getClass() == Player.class)
		{
			begin_healer_player(fixtureB, fixtureA);
		}
		
		// Damager <-> Player
		if(fixtureA.getUserData().getClass() == Damager.class &&
			fixtureB.getUserData().getClass() == Player.class)
		{
			((Player)fixtureB.getUserData()).startDamage();
		}
		if(fixtureB.getUserData().getClass() == Damager.class &&
			fixtureA.getUserData().getClass() == Player.class)
		{
			((Player)fixtureA.getUserData()).startDamage();
		}
		
		// Pickup <-> Player
		if(fixtureA.getUserData().getClass() == Pickup.class &&
				fixtureB.getUserData().getClass() == Player.class)
		{
			begin_pickup_player(fixtureA, fixtureB);
		}
		if(fixtureB.getUserData().getClass() == Pickup.class &&
				fixtureA.getUserData().getClass() == Player.class)
		{
			begin_pickup_player(fixtureB, fixtureA);
		}
	}

	@Override
	public void endContact(Contact contact)
	{
		Fixture fixtureA = contact.getFixtureA(); 
		Fixture fixtureB = contact.getFixtureB(); 
		
		// Happens from time to time
		if(fixtureA == null || fixtureB == null) return;
		
		// Healer <-> Player
		if(fixtureA.getUserData().getClass() == Healer.class &&
			fixtureB.getUserData().getClass() == Player.class)
		{
			((Player)fixtureB.getUserData()).disableTurret();
			((Player)fixtureB.getUserData()).stopHeal();
		}
		if(fixtureB.getUserData().getClass() == Healer.class &&
			fixtureA.getUserData().getClass() == Player.class)
		{
			((Player)fixtureA.getUserData()).disableTurret();
			((Player)fixtureA.getUserData()).stopHeal();
		}
		
		// Damager <-> Player
		if(fixtureA.getUserData().getClass() == Damager.class &&
			fixtureB.getUserData().getClass() == Player.class)
		{
			((Player)fixtureB.getUserData()).stopDamage();
		}
		if(fixtureB.getUserData().getClass() == Damager.class &&
			fixtureA.getUserData().getClass() == Player.class)
		{
			((Player)fixtureA.getUserData()).stopDamage();
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) 
	{
		
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) 
	{

	}
	
	private void bulletHit(Fixture fixture, Fixture object)
	{
		Bullet bullet = ((Bullet)fixture.getUserData());
		// If NOT turret hit
		if(!object.getUserData().toString().matches(".*_turret"))
		{		
			// If player hit
			if(object.getUserData().getClass() == Player.class)
			{
				Player player = (Player)object.getUserData();
				
				if(!player.isDead())
				{
					// Prevent friendly fire
					if(!player.equals(bullet.getPlayer()))
					{
						if(!player.isInvictible())
							player.decreaseHealth(Constants.BULLET_DAMAGE);
						bullet.setDead();
						// Frag to bullet's owner if player is dead after health decreasing
						if(player.getHealth() <= 0 && bullet.getPlayer() != null)
							bullet.getPlayer().increaseKills();
					}
				}
			}
			else
				bullet.setHit();
		}
		else
			if(!object.isSensor())
				bullet.setHit();
	}

	private void begin_healer_player(Fixture healerFixture, Fixture playerFixture)
	{
		Player player = (Player)playerFixture.getUserData();
		Healer healer = (Healer)healerFixture.getUserData();
		
		if(healer.isEmpty())
		{
			player.enableTurret();
			player.startHeal();
			player.setHealer(healer);
		}
	}

	private void begin_pickup_player(Fixture pickupFixture, Fixture playerFixture)
	{
		Pickup pickup = (Pickup)pickupFixture.getUserData();
		Player player = (Player)playerFixture.getUserData();
		
		if(pickup.getType().equals(PickupType.INVICTUS))
			player.setInvictible();
		else if(pickup.getType().equals(PickupType.WEAPON))
			player.setFastCooldown();
		
		pickup.setPickedUp();
	}
}
