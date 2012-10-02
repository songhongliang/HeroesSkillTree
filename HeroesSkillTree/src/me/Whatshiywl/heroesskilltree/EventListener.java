package me.Whatshiywl.heroesskilltree;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.herocraftonline.heroes.api.events.*;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;

public class EventListener implements Listener
{
	private static HeroesSkillTree plugin;
	public EventListener(HeroesSkillTree instance) 
	{
		EventListener.plugin = instance;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		Hero hero = plugin.heroes.getCharacterManager().getHero(player);
		plugin.loadPlayer(player);
		plugin.savePlayer(player);
		for(Skill skill : plugin.heroes.getSkillManager().getSkills()){
			if(plugin.isLocked(hero, skill)) if(hero.hasEffect(skill.getName())){
				//hero.getPlayer().sendMessage("Removing Effect");
				hero.removeEffect(hero.getEffect(skill.getName()));
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onLevelChangeEvent(HeroChangeLevelEvent event)
	{
		Hero hero = event.getHero();
		plugin.setPlayerPoints(hero, plugin.getPlayerPoints(hero) + (event.getTo() - event.getFrom()));
		plugin.savePlayer(hero.getPlayer());
		for(Skill skill : plugin.heroes.getSkillManager().getSkills()){
			if(plugin.isLocked(hero, skill)) if(hero.hasEffect(skill.getName())){
				//hero.getPlayer().sendMessage("Removing Effect");
				hero.removeEffect(hero.getEffect(skill.getName()));
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onClassChangeEvent(ClassChangeEvent event)
	{
		Hero hero = event.getHero();
		if(event.getTo().isDefault()){
			plugin.setPlayerPoints(hero, 0);
		}
		plugin.recalcPlayer(hero.getPlayer(), event.getTo());
		for(Skill skill : plugin.heroes.getSkillManager().getSkills()){
			if(plugin.isLocked(hero, skill)) if(hero.hasEffect(skill.getName())){
				//hero.getPlayer().sendMessage("Removing Effect");
				hero.removeEffect(hero.getEffect(skill.getName()));
			}
		}
	}
	
	//@SuppressWarnings("static-access")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerUseSkill(SkillUseEvent event){
		Hero hero = event.getHero();
		Skill skill = event.getSkill();
		if(plugin.isLocked(event.getHero(), event.getSkill()) && !event.getPlayer().hasPermission("skilltree.override.locked")){
			event.getPlayer().sendMessage(ChatColor.RED + "This skill is still locked!");
			event.getHero().hasEffect(event.getSkill().getName());
			event.setCancelled(true);
		}
		else{
			//HEALTH
			int health = (int) ((SkillConfigManager.getUseSetting(hero, skill, "hst-health", 0.0, false)) * 
					(plugin.getSkillLevel(hero, skill) - 1));
			health = health > 0 ? health : 0;
			event.setHealthCost(event.getHealthCost() + health);
			
			//MANA
			int mana = (int) ((SkillConfigManager.getUseSetting(hero, skill, "hst-mana", 0.0, false)) * 
					(plugin.getSkillLevel(hero, skill) - 1));
			mana = mana > 0 ? mana : 0;
			event.setManaCost(event.getManaCost() + mana);
			
			//REAGENT
			int reagent = (int) ((SkillConfigManager.getUseSetting(hero, skill, "hst-reagent", 0.0, false)) *
					(plugin.getSkillLevel(hero, skill) - 1));
			reagent = reagent > 0 ? reagent : 0;
			
			event.getReagentCost().setAmount(event.getReagentCost().getAmount() + reagent);
			event.setReagentCost(event.getReagentCost());
			
			//STAMINA
			int stamina = (int) (SkillConfigManager.getUseSetting(hero, skill, "hst-stamina", 0.0, false) *
					plugin.getSkillLevel(hero, skill) - 1);
			stamina = stamina > 0 ? stamina : 0;
			event.setStaminaCost(event.getStaminaCost() + stamina);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onWeaponDamge (WeaponDamageEvent event){
		if(event.getDamager() instanceof Hero){
			Hero hero = (Hero) event.getDamager();
			for(Skill skill : plugin.heroes.getSkillManager().getSkills()){
				if(plugin.isLocked(hero, skill)) if(hero.hasEffect(skill.getName())){
					/**
					 * Idea for removing passive skills:
					 * Instead of trying to remove the effect, set the damage or whatever the skill does
					 * to "normal" if the level of the skill equals 0, since they won't be able to
					 * unlock it anyways. Ofc, test if the hero has that effect to be sure they are
					 * attempting to use that passive skill.
					 */
					//hero.getPlayer().sendMessage("Removing Effect");
					hero.removeEffect(hero.getEffect(skill.getName()));
				}
				else if(hero.hasEffect(skill.getName())){
					//DAMAGE
					int damage = (int) ((SkillConfigManager.getUseSetting(hero, skill, "hst-damage", 0.0, false)) * 
							(plugin.getSkillLevel(hero, skill) - 1));
					damage = damage > 0 ? damage : 0;
					event.setDamage(event.getDamage() + damage);
				}
			}
		}
	}
}