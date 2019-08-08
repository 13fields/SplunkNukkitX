package loggerx;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDeathEvent;
import cn.nukkit.event.entity.EntityExplodeEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;



public class Main extends PluginBase implements Listener {

	private static final String AT_X_Y_Z = " at [x] [y] [z]";
    Config c;

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        c = getConfig();
        new Logger(System.getProperty("user.dir") + c.getString("logFile", "/logs/events.log"));
        if (c.getBoolean("logLoggerStatus"))
            Logger.get.print("Logging started: Logger starting up", null);
    }

    public void onDisable() {
        if (c.getBoolean("logLoggerStatus"))
            Logger.get.print("Logging stopped: Logger shutting down", null);
    }

    @EventHandler
    public void logBreak(BlockBreakEvent e) {
        if (!e.isCancelled() && c.getBoolean("logBlockBreak")) {
            Logger.get.print(e.getPlayer().getName() + " broke block " + e.getBlock().getName() + " ("
                    + e.getBlock().getId() + ":" + e.getBlock().getDamage() + ") " + AT_X_Y_Z,
                    e.getBlock().getLocation());
        }
    }

    @EventHandler
    public void logPlace(BlockPlaceEvent e) {
        if (!e.isCancelled() && c.getBoolean("logBlockPlace")) {
            Logger.get.print(e.getPlayer().getName() + " placed block " + e.getBlock().getName() + " ("
                    + e.getBlock().getId() + ":" + e.getBlock().getDamage() + ") " + AT_X_Y_Z,
                    e.getBlock().getLocation());
        }
    }

    @EventHandler
    public void logDeath(PlayerDeathEvent e) {
        if (!e.isCancelled() && c.getBoolean("logPlayerDeath")) {
            String deathMessage = "";
            String playerName = e.getEntity().getName();
            // Get the default death message
            String message = this.convertConfigTags(String.valueOf(this.c.get("CUSTOM")), playerName);

            // Get a list of damage cause
            EntityDamageEvent ev = e.getEntity().getLastDamageCause();
            DamageCause cause = e.getEntity().getPlayer().getLastDamageCause().getCause();
            
    // This part is for entity attack entity
    if (ev instanceof EntityDamageByEntityEvent) {
        Entity damager = ((EntityDamageByEntityEvent) ev).getDamager();
  
        // This condition check is to prevent class cast exception caused by mob attack
        if ((damager instanceof Player) && !(cause == DamageCause.PROJECTILE)) {
          String itemName = ((Player) damager).getInventory().getItemInHand().getName();
          message = this.convertConfigTags(this.c.getString("KILL_BY_WEAPON"), playerName,
          damager.getName(), itemName);
        } else if (cause == DamageCause.ENTITY_EXPLOSION) {
          deathMessage = this.c.getString("ENTITY_EXPLOSION");
          message = this.convertConfigTags(deathMessage, playerName, damager.getName());
        } else if (cause == DamageCause.PROJECTILE) {
          deathMessage = this.c.getString("PROJECTILE");
          message = this.convertConfigTags(deathMessage, playerName, damager.getName());
          // Mob attack
        } else if (cause == DamageCause.LIGHTNING) {
          deathMessage = this.c.getString("LIGHTNING");
          message = this.convertConfigTags(deathMessage, playerName, damager.getName());
        } else if (cause == DamageCause.ENTITY_ATTACK && !(damager instanceof Player)) {
          deathMessage = this.c.getString("MOB_ATTACK");
          message = this.convertConfigTags(deathMessage, playerName, damager.getName());
        }
      } else {
        message = this.getDeathMessage(cause, playerName);
    }
      String finalMsg = message;
      e.setDeathMessage(TextFormat.RED + finalMsg);
      Logger.get.print(message + AT_X_Y_Z, e.getEntity().getLocation());

        }
    }


    @EventHandler 
    public void playerDamage(EntityDamageByEntityEvent e) {
        if (!e.isCancelled() && c.getBoolean("logPlayerDamage"))  {
        if (e.getEntity() instanceof Player) {
        // what entity was damaged? 
            String playerName = e.getEntity().getName();
        // Get the default death message
            String damageMessagePlayer = "";
            String messagePlayer = this.convertConfigTagsPlayer(String.valueOf(this.c.get("PLAYER_DAMAGE")), playerName);
        // what caused it? not currently used 
                   // who did the damage and with what? 

            if ( (e.getEntity().getHealth() ) - e.getDamage() >= 0) {
                
                damageMessagePlayer = this.c.getString("PLAYER_ATTACK_DAMAGE");
                messagePlayer = this.convertConfigTagsPlayer(damageMessagePlayer, playerName, e.getDamager().getName());
            } else {

            }    
             Logger.get.print(messagePlayer + AT_X_Y_Z, e.getEntity().getLocation());
            }
        } 
    }
         

    //////////

    @EventHandler 
    public void entityDamage(EntityDamageByEntityEvent e) {
        if (!e.isCancelled() && c.getBoolean("logEntityDeath"))  {
        if(!(e.getEntity() instanceof Player)) {
        // what entity was damaged? 
            String entityName = e.getEntity().getName();
        // Get the default death message
            String deathMessageEntity = "";
            String messageEntity = this.convertConfigTagsEntity(String.valueOf(this.c.get("MOB_CUSTOM")), entityName);
        // what caused it? not currently used 
           
            Float health = e.getEntity().getHealth();
        // who did the damage and with what? 
            Entity damager = e.getDamager();

            String itemName = ((Player) damager).getInventory().getItemInHand().getName();

            if(((health - e.getDamage()) <= 0) && (!(itemName == null | itemName.length() == 0))) {
                messageEntity = this.convertConfigTagsEntity(this.c.getString("MOB_KILL_BY_WEAPON"), entityName, damager.getName(), itemName);
                    
            } else if(((health - e.getDamage()) <= 0) && (itemName == null | itemName.length() == 0)) {
                deathMessageEntity = this.c.getString("MOB_DEATH");
                messageEntity = this.convertConfigTagsEntity(deathMessageEntity, entityName);
                  
            } else if ((health - e.getDamage()) <= 0) {
                DamageCause causeEntity = e.getEntity().getLastDamageCause().getCause();
                messageEntity = this.getDeathMessageEntity(causeEntity, entityName);

            } else if(((health - e.getDamage()) >= 0) &&  (!(itemName == null | itemName.length() == 0))) {
                deathMessageEntity = this.c.getString("MOB_DAMAGE_BY_WEAPON");
                messageEntity = this.convertConfigTagsEntity(deathMessageEntity, entityName, damager.getName(), itemName);

            } else if (((health - e.getDamage()) >= 0) && (!(itemName == "Air" | itemName.length() == 3))) {
                deathMessageEntity = this.c.getString("MOB_DAMAGE");
                messageEntity = this.convertConfigTagsEntity(deathMessageEntity, entityName, damager.getName());
            } else {
                
            }  
            Logger.get.print(messageEntity + AT_X_Y_Z, e.getEntity().getLocation());

            } else {

            }
        }
    }                         
    

    @EventHandler
    public void logJoin(PlayerJoinEvent e) {
        if (c.getBoolean("logPlayerJoin")) {
            Logger.get.print(e.getPlayer().getName() + " decided to join in", null);
        }
    }

    @EventHandler
    public void logQuit(PlayerQuitEvent e) {
        if (c.getBoolean("logPlayerQuit")) {
            Logger.get.print(e.getPlayer().getName() + " has left: " + e.getReason(), null); 
        }
    }

    @EventHandler
    public void logDrop(PlayerDropItemEvent e) {
        if (!e.isCancelled() && c.getBoolean("logItemDrop")) {
            Logger.get.print(e.getPlayer().getName() + " dropped item " + e.getItem().getName() + " ("
                    + e.getItem().getId() + AT_X_Y_Z, e.getPlayer().getLocation());
        }
    }

    @EventHandler
    public void logFill(PlayerBucketFillEvent e) {
        if (!e.isCancelled() && c.getBoolean("logBucketFill")) {
            Logger.get.print(e.getPlayer().getName() + " filled bucket" + AT_X_Y_Z, e.getBlockClicked().getLocation());
        }
    }

    @EventHandler
    public void logEmpty(PlayerBucketEmptyEvent e) {
        if (!e.isCancelled() && c.getBoolean("logBucketEmpty")) {
            Logger.get.print(e.getPlayer().getName() + " emptied bucket" + AT_X_Y_Z, e.getBlockClicked().getLocation());
        }
    }

    @EventHandler
    public void logCommand(PlayerCommandPreprocessEvent e) {
        if (!e.isCancelled() && c.getBoolean("logPlayerCommand")) {
            Logger.get.print(e.getPlayer().getName() + " ran command " + e.getMessage() + AT_X_Y_Z, e.getPlayer().getLocation());
        }
    }



    private String textFromContainer(TextContainer container) {
        if (container instanceof TranslationContainer) {
            return getServer().getLanguage().translateString(container.getText(), ((TranslationContainer) container).getParameters());
        } else {
            return container.getText();
        }
    }

  /**
   * This method determine the death message of each death case, including the name of the victim.
   * @param cause
   * @return the death message for different cases except ENTITY_EXPLOSION, ENTITY_ATTACK, and PROJECTILE
   */
  public String getDeathMessage(DamageCause cause, String playerName) {
    String deathMessage;
    switch (cause) {
      case SUFFOCATION:
        deathMessage = this.c.getString("SUFFOCATION");
        break;
      case FALL:
        deathMessage = this.c.getString("FALL");
        break;
      case FIRE:
        deathMessage = this.c.getString("FIRE");
        break;
      case FIRE_TICK:
        deathMessage = this.c.getString("FIRE_TICK");
        break;
      case LAVA:
        deathMessage = this.c.getString("LAVA");
        break;
      case DROWNING:
        deathMessage = this.c.getString("DROWNING");
        break;
      case BLOCK_EXPLOSION:
        deathMessage = this.c.getString("BLOCK_EXPLOSION");
        break;
      case VOID:
        deathMessage = this.c.getString("VOID");
        break;
      case SUICIDE:
        deathMessage = this.c.getString("SUICIDE");
        break;
      case MAGIC:
        deathMessage = this.c.getString("MAGIC");
        break;
      default:
        deathMessage = this.c.getString("CUSTOM");
    }
    String result = this.convertConfigTags(deathMessage, playerName);
    return result;
  }

  /**
   * This method determine the death message of each death case, including the name of the victim.
   * @param causeEntity
   * @return the death message for different cases except ENTITY_EXPLOSION, ENTITY_ATTACK, and PROJECTILE
   */
  public String getDeathMessageEntity(DamageCause causeEntity, String entityName) {
    String deathMessageEntity;
    switch (causeEntity) {
      default:
      deathMessageEntity = this.c.getString("MOB_DEATH");
    }
    String result = this.convertConfigTags(deathMessageEntity, entityName);
    return result;
  }


/**
   * This method will convert configuration tags to variables in a provided String
   * 
   * @param String deathMessage
   * @param String deathMessageEntity
   * @param String damageMessagePlayer
   * @param String entityName
   * @param String playerName
   * @param String weaponName
   * @param String Attacker
   * @param String 

   */
  public String convertConfigTags(String deathMessage, String playerName, String Attacker,
      String weaponName) {
    String newDeathMessage = "";
    newDeathMessage = deathMessage.replace("<Player>", playerName);
    newDeathMessage = newDeathMessage.replace("<Attacker>", Attacker);
    newDeathMessage = newDeathMessage.replace("<WeaponName>", weaponName);
    return newDeathMessage;
  }

  public String convertConfigTags(String deathMessage, String playerName, String Attacker) {
    String newDeathMessage = "";
    newDeathMessage = deathMessage.replace("<Player>", playerName);
    newDeathMessage = newDeathMessage.replace("<Attacker>", Attacker);
    return newDeathMessage;
  }

  public String convertConfigTags(String deathMessage, String playerName) {
    String newDeathMessage = "";
    newDeathMessage = deathMessage.replace("<Player>", playerName);
    return newDeathMessage;
  }

  public String convertConfigTagsEntity(String deathMessageEntity, String entityName, String Attacker,
      String weaponName) {
    String newDeathMessageEntity = "";
    newDeathMessageEntity = deathMessageEntity.replace("<Entity>", entityName);
    newDeathMessageEntity = newDeathMessageEntity.replace("<Attacker>", Attacker);
    newDeathMessageEntity = newDeathMessageEntity.replace("<WeaponName>", weaponName);
    return newDeathMessageEntity;
  }

  public String convertConfigTagsEntity(String deathMessageEntity, String entityName, String Attacker) {
    String newDeathMessageEntity = "";
    newDeathMessageEntity = deathMessageEntity.replace("<Entity>", entityName);
    newDeathMessageEntity = newDeathMessageEntity.replace("<Attacker>", Attacker);
    return newDeathMessageEntity;
  }

  public String convertConfigTagsEntity(String deathMessageEntity, String entityName) {
    String newDeathMessageEntity = "";
    newDeathMessageEntity = deathMessageEntity.replace("<Entity>", entityName);
    return newDeathMessageEntity;
  }

  public String convertConfigTagsPlayer(String damageMessagePlayer, String entityName, String Attacker) {
    String newDamageMessagePlayer = "";
    newDamageMessagePlayer = damageMessagePlayer.replace("<Player>", entityName);
    newDamageMessagePlayer = newDamageMessagePlayer.replace("<Attacker>", Attacker);
    return newDamageMessagePlayer;
  }

  public String convertConfigTagsPlayer(String damageMessagePlayer, String entityName) {
    String newDamageMessagePlayer = "";
    newDamageMessagePlayer = damageMessagePlayer.replace("<Player>", entityName);
    return newDamageMessagePlayer;
  }


}
