package com.Moon_Eclipse.MCMANYOreGen;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.nossr50.api.ExperienceAPI;

public class OreGen extends JavaPlugin implements Listener{
	Configuration c;
	private FileConfiguration item;
	private File itemfile;
	Random rnd = new Random();
	
	public void onEnable()
	{
		Bukkit.getPluginManager().registerEvents(this, this);
		this.saveDefaultConfig();
		this.saveDefaultitem();
		c = this.getConfig();
		itemfile = new File(getDataFolder(), "item.yml");
		item = YamlConfiguration.loadConfiguration(itemfile);
	}
	public void onDisable()
	{
		
	}
	public boolean onCommand(CommandSender sender, Command command, String Label, String[] args)
	{
		if(command.getName().equals("광산"))
		{
			if(args[0].equals("리로드"))
			{
				this.reloadConfig();
				c = this.getConfig();
				itemfile = new File(getDataFolder(), "item.yml");
				item = YamlConfiguration.loadConfiguration(itemfile);
				sender.sendMessage("MCMANYOreGen 리로드 완료. 버전: 1.1v[07.16]");
			}
			if (args[0].equals("받기"))
			{
				// 광산 받기 플레이어이름 아이템이름 갯수
			    String itemname = args[2];
			    int getamount = Integer.parseInt(args[3]);
			    String playername = args[1];
			    Player targetPlayer = Bukkit.getPlayer(playername);
			    if (!itemname.equals(""))
				{
				    if (getamount != 0)
				    {
				    	if(targetPlayer.isOnline())
				    	{
				    		String key = "item." + itemname;
						    List<String> lore = item.getStringList(key + ".lore");
						    for (int i = 0; i < lore.size(); i++)
						    {
							    String s = lore.get(i).replace("&", "§");
							    String s2 = s.replaceAll("PLAYER", sender.getName());
							    lore.set(i, s2);
						    }
						    ItemStack is = this.createItem(c.getInt(key + ".id"), c.getInt(key + ".metadata"), getamount, c.getString(key + ".name").replace("&", "§"), lore, c.getString(key + ".color"),  c.getStringList(key + ".enchants"));
						    targetPlayer.getInventory().addItem(is);
				    	}
				    	else
				    	{
				    		sender.sendMessage("대상이 접속해 있지 않습니다.");
				    	}
				    }
				    else
				    {
				    	sender.sendMessage("갯수는 0개 이하일 수 없습니다.");
				    }
				}
			    else
			    {
			    sender.sendMessage("정확한 이름을 입력해 주세요.");
			    }
			}
		}
		return true;
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBreakBlock(BlockBreakEvent event)
	{
		if(event.isCancelled() == false)
		{
			Location loc = event.getBlock().getLocation();
			int getbreakblock = event.getBlock().getTypeId();
			int getbreakmeta = event.getBlock().getData();
			Player p = event.getPlayer();
			ItemStack hand = p.getItemInHand();
			String worldname = p.getWorld().getName();
			
			Set<String> worldlist = c.getConfigurationSection("config").getKeys(false);

			if(worldlist.contains(worldname))
			{
				String key = "config." + worldname + ".";
				List<String> replaceto = c.getStringList(key + getbreakblock + "*" + getbreakmeta + ".replaceto");
				if(!(replaceto.isEmpty()) && !(replaceto.get(0).equals("")))
				{
					key = key + getbreakblock + "*" + getbreakmeta;
					ItemStack dropitem = new ItemStack(0);
					int rnd_per = rnd.nextInt((100) + 1);
					int[] replaceitem = this.replaceint(replaceto, rnd_per);
					int replaceid = replaceitem[0];
					int replacemeta = replaceitem[1];
					long delay = c.getLong(key + ".delay");
					List<String> potion = c.getStringList(key + ".potion");
					List<String> commands = c.getStringList(key + ".commands");
					List<String> drop_itemMeta = c.getStringList(key + ".drop_itemMeta");
					int dropexp = c.getInt(key + ".dropexp");
					event.setExpToDrop(dropexp);
					
					if(c.getString("config.UseMCMMO").equalsIgnoreCase("true"))
					{
						ExperienceAPI.addXP(p, "Mining", c.getInt(key + ".mmoxp"));
					}
					if(!(potion.isEmpty()) && !(potion.get(0).equals("")))
					{
						for(String po : potion)
						{
							int effectnumber = Integer.parseInt(po.substring(0, po.indexOf(",")));
							int second = Integer.parseInt(po.substring(po.indexOf(":") +1, po.length()));
							int level = Integer.parseInt(po.substring(po.indexOf(",") +2, po.indexOf(":")));
							p.addPotionEffect(new PotionEffect(this.int2PotionEffect(effectnumber), second * 20, level));
						}
					}
					if(!(commands.isEmpty()) && !(commands.get(0).equals("")))
					{
						for(String co : commands)
						{
							String colorcommand = co.replace("&", "§");
							String finalcommand = colorcommand.replaceAll("PLAYER", p.getName());
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalcommand);
						}
					}
					
					if(!(drop_itemMeta.isEmpty()) && !(drop_itemMeta.get(0).equals("")))
					{ 
						int drop_per = rnd.nextInt((100) + 1);
						String dropitemname = this.dropitem(drop_itemMeta, drop_per);
						key = "item." + dropitemname;
						
						int id = item.getInt(key + ".id");
						int meta = item.getInt(key + ".metadata");
						int amount = item.getInt(key + ".amount");
						String name = item.getString(key + ".name");
						List<String> lore = item.getStringList(key + ".lore");
						List<String> enchants = item.getStringList(key + ".enchants");
						
						dropitem = this.createItem(id, amount, name, lore, meta, enchants);

						
						event.setCancelled(true);
						((ExperienceOrb)event.getBlock().getWorld().spawn(loc, ExperienceOrb.class)).setExperience(dropexp);
						Bukkit.getWorld(worldname).dropItemNaturally(loc, dropitem);
						Bukkit.getWorld(worldname).getBlockAt(loc).setTypeIdAndData(0, (byte)0, true);
						Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable()
						{
							public void run()
							{
								Bukkit.getWorld(worldname).getBlockAt(loc).setTypeIdAndData(replaceid, (byte)replacemeta, true);
							}
						}, delay);
						
					}
					else
					{
						event.setCancelled(true);
						event.getBlock().breakNaturally();
						
					}
					if(this.CanDownDura(hand))
					{
						short durability = hand.getDurability();
						int getdowndura = c.getInt("config." + worldname + "." + getbreakblock + ".durability");
						short setdura = (short) (durability + getdowndura);
						short maxdura = hand.getType().getMaxDurability();
						short getdura = hand.getDurability();
						
						if (setdura > maxdura)
						{
							p.getInventory().clear(p.getInventory().getHeldItemSlot());	
						}
						else
						{
							hand.setDurability(setdura);
						}
					}
				}
			}
		}
	}
	public ItemStack createItem(int typeId, int amount, String name, List<String> lore, int meta, List<String> enchants)
	{
		ItemStack i = new ItemStack(typeId, amount,(short) 0,(byte) meta);
		ItemMeta im = i.getItemMeta();
		ArrayList<String> newlore= new ArrayList<>();
		
		if(!((name+"").equalsIgnoreCase("null")))
		{
			String displayname = name.replace("&", "§");
			im.setDisplayName(displayname);
		}
		
		if(!(lore.isEmpty()) && !(lore.get(0).equals("")))
		{
			for(String l : lore)
			{
				String colored_lore = l.replace("&", "§");
				newlore.add(colored_lore);
			}
			
			im.setLore(newlore);
		}	
		i.setItemMeta(im);
		if(!(enchants.isEmpty()) && !(enchants.get(0).equals("")))
		{
			for(String enchant : enchants)
			{
				//'16: 1'
				int enchantname = Integer.parseInt(enchant.substring(0, enchant.indexOf(":")));
				int level = Integer.parseInt(enchant.substring(enchant.indexOf(":") + 2));
				i.addUnsafeEnchantment(Enchantment.getById(enchantname), level);
			}
		}
		
		return i;
	}
	public int[] replaceint (List<String> replaceto , int rnd)
	{
		int i[] = new int[2];
		int range = 0;
		for(String st : replaceto)
		{
			i[0] = Integer.parseInt(st.substring(0, st.indexOf(":")));
			i[1] = Integer.parseInt(st.substring(st.indexOf(":") + 1, st.indexOf(",")));
			int replace_per = Integer.parseInt(st.substring(st.indexOf(",") + 2, st.indexOf("%")));
			range += replace_per;
			if(rnd <= range)
			{
				return i;
			}
		}
		return i;
	}
	public String dropitem (List<String> dropitems , int rnd)
	{
		String str = "";
		int range = 0;
		for(String st : dropitems)
		{
			str = st.substring(0, st.indexOf(","));
			int drop_per = Integer.parseInt(st.substring(st.indexOf(",") + 2, st.indexOf("%")));
			range += drop_per;
			if(rnd <= range)
			{
				return str;
			}
		}
		return str;
	}
	public PotionEffectType int2PotionEffect(int i)
	{
		PotionEffectType ef = PotionEffectType.SPEED;
		switch(i)
		{
		//"신속", "구속", "성급함", "피로", "힘" , "즉시 회복", "즉시 데미지", "점프 강화", "멀미", "재생", "저항", "화염저항", "수중 호흡", "투명화", "실명", "야간투시", "허기", "나약함", "독", "위더"
			case 1:
				ef = PotionEffectType.SPEED;
			break;
			case 2:
				ef = PotionEffectType.SLOW;
			break;
			case 3:
				ef = PotionEffectType.FAST_DIGGING;
			break;
			case 4:
				ef = PotionEffectType.SLOW_DIGGING;
			break;
			case 5:
				ef = PotionEffectType.INCREASE_DAMAGE;
			break;
			case 6:
				ef = PotionEffectType.HEAL;
			break;
			case 7:
				ef = PotionEffectType.HARM;
			break;
			case 8:
				ef = PotionEffectType.JUMP;
			break;
			case 9:
				ef = PotionEffectType.CONFUSION;
			break;
			case 10:
				ef = PotionEffectType.REGENERATION;
			break;
			case 11:
				ef = PotionEffectType.DAMAGE_RESISTANCE;
			break;
			case 12:
				ef = PotionEffectType.FIRE_RESISTANCE;
			break;
			case 13:
				ef = PotionEffectType.WATER_BREATHING;
			break;
			case 14:
				ef = PotionEffectType.INVISIBILITY;
			break;
			case 15:
				ef = PotionEffectType.BLINDNESS;
			break;
			case 16:
				ef = PotionEffectType.NIGHT_VISION;
			break;
			case 17:
				ef = PotionEffectType.HUNGER;
			break;
			case 18:
				ef = PotionEffectType.WEAKNESS;
			break;
			case 19:
				ef = PotionEffectType.POISON;
			break;
			case 20:
				ef = PotionEffectType.WITHER;
			break;
		}
		return ef;
	}
	public void saveDefaultitem()
	{
		   if (itemfile == null)
		   {
			   itemfile = new File(getDataFolder(), "item.yml");
		   }
		   if (!itemfile.exists())
		   {            
			   this.saveResource("item.yml", true);
		   }
	}
	public void saveTitle()
	{
		try 
		{
	       item.save(itemfile);
	    }
		catch (IOException ex)
		{
	        getLogger().log(Level.SEVERE, "Could not save config to " + itemfile, ex);
	    }
	}
	public boolean CanDownDura(ItemStack item)
	{
		boolean b = false;
		int[] tools ={256,257,258,269,270,271,273,274,275,277,278,279,284,285,286,290,267,268,272,276,283}; 
		int id = item.getTypeId();
		for(int i : tools)
		{
			if(i == id)
			{
				b = true;
				break;
			}
		}
		return b;
	}
	public boolean IsThatPickaxe(ItemStack item)
	{
		boolean b = false;
		int[] tools ={270, 274, 257, 285, 278}; 
		int id = item.getTypeId();
		for(int i : tools)
		{
			if(i == id)
			{
				b = true;
				break;
			}
		}
		return b;
	}
	public boolean hasString(List<String> lore, String name)
	{
		boolean b = false;
		for(String search : lore)
		{
			if(search.contains(name))
			{
				b = true;
			}
		}
		return b;
	}
	public ItemStack createItem(int typeId,int metadata,  int amount, String name, List<String> lore, String color, List<String> enchants)
	{
		ItemStack i = new ItemStack(typeId, 1,(short) 0,(byte) metadata);
		ItemMeta im = i.getItemMeta();
		String ColorHex = color;
		try
		{
			if(typeId == 298 || typeId == 299 || typeId == 300 || typeId == 301)
			{
				LeatherArmorMeta im2 = (LeatherArmorMeta) im;
				im2.setColor(Color.fromRGB(Integer.parseInt(ColorHex, 16)));
			}
		}catch(Exception e){}
		im.setDisplayName(name);
		im.setLore(lore);
		i.setItemMeta(im);
		Random rnd = new Random();
		if(!(enchants.isEmpty()))
		{
			for(String enchant : enchants)
			{
				//'16: 1'
				int enchantname = Integer.parseInt(enchant.substring(0, enchant.length() - 3));
				int level = Integer.parseInt(enchant.substring(enchant.length() - 1));
				i.addUnsafeEnchantment(Enchantment.getById(enchantname), level);
			}
		}
		return i;
	}

}
