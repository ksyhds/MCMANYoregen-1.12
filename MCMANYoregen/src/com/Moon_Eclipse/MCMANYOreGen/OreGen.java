package com.Moon_Eclipse.MCMANYOreGen;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.Moon_Eclipse.MCgive.main;
import com.Moon_eclipse.EclipseLib.LibMain;

public class OreGen extends JavaPlugin implements Listener{
	Configuration c;
	
	HashMap<String, String> PlayerClickLeft = new HashMap<String, String>();
	HashMap<String, String> PlayerClickRight = new HashMap<String, String>();
	HashMap<String, Boolean> GetLocation = new HashMap<String, Boolean>();
	
	private FileConfiguration OreGroups;
	private File OreGroupsfile;
	
	private FileConfiguration locations;
	private File locationsFile;
	
	private FileConfiguration items;
	private File itemsFile;
	
	Random rnd = new Random();
	
	public void onEnable()
	{
		Bukkit.getPluginManager().registerEvents(this, this);
		
		this.saveDefaultConfig();
		this.saveDefaultOreGroups();
		this.saveDefaultlocations();
		this.saveDefaultitems();
		
		c = this.getConfig();
		OreGroupsfile = new File(getDataFolder(), "OreGroups.yml");
		OreGroups = YamlConfiguration.loadConfiguration(OreGroupsfile);
		locationsFile = new File(getDataFolder(), "locations.yml");
		locations = YamlConfiguration.loadConfiguration(locationsFile);
		itemsFile = new File(getDataFolder(), "items.yml");
		items = YamlConfiguration.loadConfiguration(itemsFile);
		
		// 광산으로 지정된 영역을 모두 돌로 초기화.
		InitializeMine();
	}
	public void onDisable()
	{
		
	}
	
	public boolean onCommand(CommandSender sender, Command command, String Label, String[] args)
	{
		if(command.getName().equals("광산") && sender.isOp())
		{
			if( args.length < 1 || args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help"))
			{	
				if(sender.isOp())
				{
					sender.sendMessage("/광산 reload");
					sender.sendMessage("/광산 받기 플레이어이름 아이템이름 갯수");
					sender.sendMessage("/광산 설정");
					sender.sendMessage("/광산 생성 광산이름 그룹이름");
					sender.sendMessage("/광산 삭제 광산이름");
					sender.sendMessage("/광산 초기화");
				}
			}
			else if(args[0].equals("리로드"))
			{
				this.reloadConfig();
				c = this.getConfig();
				
				OreGroupsfile = new File(getDataFolder(), "item.yml");
				OreGroups = YamlConfiguration.loadConfiguration(OreGroupsfile);
				
				locationsFile = new File(getDataFolder(), "locations.yml");
				locations = YamlConfiguration.loadConfiguration(locationsFile);
				
				itemsFile = new File(getDataFolder(), "items.yml");
				items = YamlConfiguration.loadConfiguration(itemsFile);
				
				sender.sendMessage("MCMANYOreGen 리로드 완료.");
			}
			else if (args[0].equals("받기"))
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
						    List<String> lore = items.getStringList(key + ".lore");
						    for (int i = 0; i < lore.size(); i++)
						    {
							    String s = lore.get(i).replace("&", "§");
							    String s2 = s.replaceAll("PLAYER", sender.getName());
							    lore.set(i, s2);
						    }
						    ItemStack is = LibMain.createItem(c.getInt(key + ".id"), c.getInt(key + ".metadata"), getamount, c.getString(key + ".name").replace("&", "§"), lore, c.getString(key + ".color"),  c.getStringList(key + ".enchants"));
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
			// 광산 설정
			else if (args[0].equals("설정"))
			{
				// 만약 커맨드 사용자가 플레이어라면
				if(sender instanceof Player) 
				{
					// 커맨드 사용자를 플레이어로 치환하여 저장
					Player p = (Player) sender;
					
					// 플레이어가 나무도끼로 광산 설정 상태가 아니라면
					if(GetLocation.get(p.getName()) == false)
					{
						GetLocation.put(sender.getName(), true);
						sender.sendMessage("나무도끼를 광산 좌표 설정 용도로 변경합니다.");
					}
					else if(GetLocation.get(sender.getName()))
					{
						GetLocation.put(sender.getName(), false);
						sender.sendMessage("나무도끼를 일반 용도로 변경합니다.");
					}
				}
				else
				{
					sender.sendMessage("only can use PLAYER. not CONSOLE");
				}
				
			}
			// 광산 생성 그룹이름
			// 만약 커맨드의 속성 0 번째가 생성 이라면
			else if(args[0].equalsIgnoreCase("생성"))
			{
				// 만약 커맨드 사용자가 플레이어라면
				if(sender instanceof Player) 
				{
					// 커맨드 사용자를 플레이어로 치환하여 저장
					Player p = (Player) sender;
					
					// 커맨드의 길이가 1보다 크다면
					if(args.length > 2) 
					{
						// pos1을 플레이어가 왼쪽 클릭한 곳으로 정함
						String Pos1 = PlayerClickLeft.get(p.getName());
						
						// pos2를 플레이어가 오른쪽 클릭한 곳으로 저장함
						String Pos2 = PlayerClickRight.get(p.getName());
						
						// args[1]을 그룹이름으로써 사용하기위해 변수에 저장
						String Group_Name = args[2];
						
						// locations.yml의 항목에 각 값을 저장함.
						locations.set("locations." + args[1] + ".pos1", Pos1);
						locations.set("locations." + args[1] + ".pos2", Pos2);
						locations.set("locations." + args[1] + ".world", p.getWorld().getName());
						locations.set("locations." + args[1] + ".group", Group_Name);
						locations.set("locations." + args[1] + ".initial_ore", "Initial_Default");
						
						// locations 문서를 저장함
						this.savelocations();
						
						// 
						
						// 플레이어에게 작업 내용을 전달
						p.sendMessage("locations 파일에 " + args[1] + "항목이 생성 되었습니다. 그룹은 \'" + Group_Name + "\' 입니다.");
						
						// 설정 모드를 해제하기 위해 설정 변경
						GetLocation.put(sender.getName(), false);
						sender.sendMessage("나무도끼를 일반 용도로 변경합니다.");
					}
					else 
					{
						p.sendMessage("인수가 모자랍니다.");
					}
				}
				else
				{
					sender.sendMessage("only can use PLAYER. not CONSOLE");
				}
			}
			else if(args[0].equalsIgnoreCase("삭제"))
			{
				if(args.length > 1) 
				{
					locations.set("locations." + args[1], null);
					this.savelocations();
					sender.sendMessage("삭제 완료.");
				}
				else 
				{
					sender.sendMessage("인수가 모자랍니다.");
				}
			}
			else if(args[0].equalsIgnoreCase("초기화"))
			{
				InitializeMine();
				sender.sendMessage("광산 초기화 완료.");
			}
		}
		return true;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		
		Player p = event.getPlayer();
		String name = p.getName();
		if(p.isOp())
		{
			PlayerClickLeft.put(name, "");
			PlayerClickRight.put(name, "");
			GetLocation.put(name, false);
		}
	}
	
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBreakBlock(BlockBreakEvent event)
	{
		Player p = event.getPlayer();
		if(p.isOp() && GetLocation.get(p.getName()))
		{
			event.setCancelled(true);

			p.sendMessage("현재 광산 편집 중 입니다. '/광산 설정' 커맨드를 통해서 편집 상태를 해제해 주세요.");
		}
		if(event.isCancelled() == false)
		{
			Location loc = event.getBlock().getLocation();
			int getbreakblock = event.getBlock().getTypeId();
			int getbreakmeta = event.getBlock().getData();
			
			ItemStack hand = p.getItemInHand();
			
			// 처리 순서
			// locations.yml 의 각 항목은 config에 등록되어있는 initial_blocks:@을 갖으며 부팅,커맨드 입력시 이것을 기준으로 초기화한다.
			
			// 블럭 브레이크
			// 이벤트가 발생한 월드의 이름을 저장한다
			String worldname = loc.getWorld().getName();
			
			// 블럭이 부숴진곳이 광산이라면 광산의 이름을 얻어옴
			String Break_Region = IsThisMinePlace_AndWhere(loc);
			
			//Bukkit.broadcastMessage("Break_Region: " + Break_Region);
			
			// 만약 해당 장소가 광산이라면
			if(!Break_Region.equals(""))
			{
				// 영역의 광산그룹을 얻어옴			
				String OreGroup = locations.getString("locations." + Break_Region + ".group");
				//Bukkit.broadcastMessage("OreGroup: " + OreGroup);
				
				//작성되어있는 아이템중 드랍 아이템을 확률적으로 얻는다.
				String OreGroups_key = "OreGroups." + OreGroup + ".";
				
				//부숴진 블럭을 대체할 블럭이 지정되어있는지 확인.
				List<String> replaceto = OreGroups.getStringList(OreGroups_key + getbreakblock + "*" + getbreakmeta + ".replaceto");
				
				//만약 대체 블럭이 존재하는 경우
				if(!(replaceto.isEmpty()) && !(replaceto.get(0).equals("")))
				{
					
					//------------------------------------ 아래는 데이터 초기화 부분
					
					// 여러개의 대체 블럭중 어느것으로 대체될지 얻어옴
					int rnd_per = rnd.nextInt((100) + 1);
					int[] replaceitem = this.replaceint(replaceto, rnd_per);
					int replaceid = replaceitem[0];
					int replacemeta = replaceitem[1];
					
					// 항목을 불러들이기 위한 key 값을 변경
					String key = OreGroups_key + getbreakblock + "*" + getbreakmeta;
					
					// 블럭이 재 생성될 시간을 불러옴
					long delay = OreGroups.getLong(key + ".delay");
					
					// 포션 이펙트들을 얻어옴
					List<String> potion = OreGroups.getStringList(key + ".potion");
					
					// 블럭이 부숴졌을 경우 실행될 커맨드들을 불러옴
					List<String> commands = OreGroups.getStringList(key + ".commands");
					
					// 드랍될 아이템들을 불러옴
					List<String> drop_itemMeta = OreGroups.getStringList(key + ".drop_itemMeta");
					
					// 드랍될 경험치를 불러옴
					int dropexp = OreGroups.getInt(key + ".dropexp");	
					
					// 드랍될 아이템을 담아둘 버퍼를 생성
					ItemStack dropitem = new ItemStack(0);
					
					//------------------------------------ 아래는 데이터 가공부분
					
					// 만약 포션이펙트의 데이터가 있다면
					if(!(potion.isEmpty()) && !(potion.get(0).equals("")))
					{
						// 적혀있는 포션이펙트를 플레이어에게 적용
						for(String po : potion)
						{
							int effectnumber = Integer.parseInt(po.substring(0, po.indexOf(",")));
							int second = Integer.parseInt(po.substring(po.indexOf(":") +1, po.length()));
							int level = Integer.parseInt(po.substring(po.indexOf(",") +2, po.indexOf(":")));
							p.addPotionEffect(new PotionEffect(this.int2PotionEffect(effectnumber), second * 20, level));
						}
					}
					
					// 만약 커맨드의 데이터가 있다면
					if(!(commands.isEmpty()) && !(commands.get(0).equals("")))
					{
						// 적혀있는 모든 커맨드를 콘솔 권한으로 실행
						for(String co : commands)
						{
							String colorcommand = co.replace("&", "§");
							String finalcommand = colorcommand.replaceAll("PLAYER", p.getName());
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalcommand);
						}
					}
					
					// 만약 드랍 아이템이 있다면
					if(!(drop_itemMeta.isEmpty()) && !(drop_itemMeta.get(0).equals("")))
					{
						// 드랍될 퍼센트를 정하기위해 랜덤 변수를 선
						int drop_per = rnd.nextInt((100) + 1);
						
						// 아이템리스트 중에서 드랍될 아이템 이름을 얻어옴.
						String dropitemname = this.dropitem(drop_itemMeta, drop_per);
						//Bukkit.broadcastMessage("dropitemname: " + dropitemname);
						
						// 아이템이름을 사용해 드랍될 아이템의 경로를 정함.
						key = "items." + dropitemname;
						
						// 아이템 갯수를 얻어옴
						int amount = items.getInt(key + ".amount");
						
						// 아이템 이름을 얻어옴. 만약 mcgive에서 아이템을 갖고 오는 경우 이것이 mcgive 항목 이름이 됨
						String name = items.getString(key + ".name");
						
						// mcgive의 아이템을 얻어올 것인지 여부를 얻어옴
						boolean getMCgive = items.getBoolean(key + "getMCgive");
						//Bukkit.broadcastMessage("getMCgive: " + getMCgive);
						
						//만약 mcgive 아이템을 얻어오는 것이라면
						if(getMCgive)
						{
							// 드랍 아이템을 mcgive에서 얻어옴
							dropitem = main.get_Mcgive_Item("", name, amount);
						}
						// mcgive 아이템을 얻어오는것이 아니라면
						else
						{							
							// 아이템 id를 얻어옴
							int id = items.getInt(key + ".id");
							
							// 아이템 data를 얻어옴. 5:3 의 3부분
							int meta = items.getInt(key + ".metadata");

							
							// 아이템 로어의 목록을 얻어옴
							List<String> lore = items.getStringList(key + ".lore");
							
							// 아이템 인챈트 목록을 얻어옴
							List<String> enchants = items.getStringList(key + ".enchants");
													
							// 플레이스 홀더의 역할을 위해서 PLAYER를 실제 플레이어 이름으로 바꿈
							lore = LibMain.ChangeString("PLAYER", p.getName(), lore);
							
							// 드랍 아이템의 각 데이터를 기반으로 itemstack을 생성함
							dropitem = this.createItem(id, amount, name, lore, meta, enchants);	
							
						}
						
						// EclipseLib을 이용해서 아이템의 내구도를 무한으로 만듬.
						//Bukkit.broadcastMessage("dropitem: " + dropitem.toString());
						dropitem = LibMain.hideFlags_Unbreak(dropitem);
						
						//일반 아이템이 드랍되지 않게 하기위해 이벤트를 취소
						event.setCancelled(true);
						
						// 만약 드랍시킬 exp가 0이 아니라면
						if(dropexp != 0)
						{
							// 블럭이 부서진 좌표에 dropexp 만큼의 경험치를 떨어트림
							((ExperienceOrb)event.getBlock().getWorld().spawn(loc, ExperienceOrb.class)).setExperience(dropexp);
						}
						
						// 드랍 아이템을 자연스럽게 블럭이 부서진 좌표에 떨어트림
						Bukkit.getWorld(worldname).dropItemNaturally(loc, dropitem);
						
						// 이벤트가 취소되었으므로 해당 좌표의 블럭을 강제적으로 부
						Bukkit.getWorld(worldname).getBlockAt(loc).setTypeIdAndData(0, (byte)0, true);
						
						// 블럭이 부숴졌으므로 일정 시간 뒤에 리젠시키기 위해 runtask를 만듬
						Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable()
						{
							@SuppressWarnings("deprecation")
							public void run()
							{
								// 일정 시간이 지나면 딱 한번 주어진 장소에 대체블럭을 생성함.
								Bukkit.getWorld(worldname).getBlockAt(loc).setTypeIdAndData(replaceid, (byte)replacemeta, false);
							}
						}, delay);
						
					}
					// 드랍 아이템이 없는 경우
					else
					{
						// 이벤트를 취소함
						event.setCancelled(true);
						
						// 블럭을 자연스럽게 부숨
						event.getBlock().breakNaturally();
						
					}
					
					// 만약 손에 든 아이템의 내구도를 깎을 수 있게 했다면
					if(this.CanDownDura(hand))
					{
						// 손에 든 아이템의 내구도 값을 얻어옴
						short durability = hand.getDurability();
						
						// 얼마나 떨어트릴지 문서에서 가져옴
						int getdowndura = c.getInt("OreGroups." + getbreakblock + "*" + getbreakmeta + ".durability");
						
						// 내구도를 설정함.
						short setdura = (short) (durability + getdowndura);
						
						// 손에 든 아이템의 최대 내구도값을 얻어옴
						short maxdura = hand.getType().getMaxDurability();
						
						// 현재 손에 든 아이템의 내구도 값을 얻어옴
						short getdura = hand.getDurability();
						
						// 만약 설정할 내구도가 최대 내구도를 넘는다면
						if (setdura > maxdura)
						{
							//손에 든 아이템을 제거 - 값이 클수록 내구도가 많이 닳은 것이므로
							p.getInventory().clear(p.getInventory().getHeldItemSlot());	
						}
						else
						{
							// 내구도를 설정
							hand.setDurability(setdura);
						}
					}
				}
				
			}
			
			// 아이템을 드랍시킨다.
			
			
			/*
			
			// 월드 이름을 검사. config.yml파일에 등록되어있지 않은 월드라면 작동하지 않음.
			if(worldlist.contains(worldname))
			{
				// 이벤트가 발생한 장소가 광산인지 아닌지 판별.
				if(IsThisMinePlace(loc)) 
				{
					if(!p.isOp())
					{
						int macroid = c.getInt("config.macro.id");
						int blockid = event.getBlock().getTypeId();
						
						if(macroid == blockid)
						{
							String command = c.getString("config.macro.command");
							Bukkit.dispatchCommand(p, command);
						}
					}
					String key = "config." + worldname + ".";
					List<String> replaceto = c.getStringList(key + getbreakblock + "*" + getbreakmeta + ".replaceto");
					if(!(replaceto.isEmpty()) && !(replaceto.get(0).equals("")))
					{
						key = key + getbreakblock + "*" + getbreakmeta;
						ItemStack dropitem = new ItemStack(0);
						
						
						List<String> potion = c.getStringList(key + ".potion");
						List<String> commands = c.getStringList(key + ".commands");
						List<String> drop_itemMeta = c.getStringList(key + ".drop_itemMeta");
						int dropexp = c.getInt(key + ".dropexp");						
						
						
					}
				}
			}
			*/
		}
	}
	@EventHandler
	public void onBlockInteract(PlayerInteractEvent event)
	{
		Player p = event.getPlayer();
		if(p.isOp())
		{
			int selector_ID = 271;
			if(GetLocation.get(p.getName()) == true && p.getItemInHand().getTypeId() == selector_ID)
			{
				Location l = event.getClickedBlock().getLocation();
				int x = l.getBlockX();
				int y = l.getBlockY();
				int z = l.getBlockZ();
				//110,113,112
				String position = x + "," + y + "," + z;
				if(event.getAction() == Action.LEFT_CLICK_BLOCK)
				{
					PlayerClickLeft.put(p.getName(), position);
					p.sendMessage("first position is " + position);
				}
				if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
				{
					PlayerClickRight.put(p.getName(), position);
					p.sendMessage("second position is " + position);
				}
				
				
			}
		}
		
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			Location loc = event.getClickedBlock().getLocation();
			String Break_Region = IsThisMinePlace_AndWhere(loc);
			if(!Break_Region.equals(""))
			{
				int blockid = event.getClickedBlock().getTypeId();
				int macroid = c.getInt("config.macro.id");
				if(macroid == blockid)
				{
					int targetid = c.getInt("config.macro.id2");
					p.getWorld().getBlockAt(loc).setTypeIdAndData(targetid, (byte)0, false);
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
	public void saveDefaultOreGroups()
	{
		   if (OreGroupsfile == null)
		   {
			   OreGroupsfile = new File(getDataFolder(), "OreGroups.yml");
		   }
		   if (!OreGroupsfile.exists())
		   {            
			   this.saveResource("OreGroups.yml", true);
		   }
	}
	public void saveDefaultitems()
	{
		   if (itemsFile == null)
		   {
			   itemsFile = new File(getDataFolder(), "items.yml");
		   }
		   if (!itemsFile.exists())
		   {            
			   this.saveResource("items.yml", true);
		   }
	}
	public void saveTitle()
	{
		try 
		{
			OreGroups.save(OreGroupsfile);
	    }
		catch (IOException ex)
		{
	        getLogger().log(Level.SEVERE, "Could not save config to " + OreGroupsfile, ex);
	    }
	}
	public void saveDefaultlocations()
	{
		   if (locationsFile == null)
		   {
			   locationsFile = new File(getDataFolder(), "locations.yml");
		   }
		   if (!locationsFile.exists())
		   {            
			   this.saveResource("locations.yml", true);
		   }
	}
	public void savelocations()
	{
		try 
		{
			locations.save(locationsFile);
	    }
		catch (IOException ex)
		{
	        getLogger().log(Level.SEVERE, "Could not save config to " + locationsFile, ex);
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
	public String IsThisMinePlace_AndWhere(Location BlockLocation)
	{
		String re = "";
		String pos1 = "";
		String pos2 = "";
		String config_world = "";
		String target_world = "";
		Set<String> keys = locations.getConfigurationSection("locations").getKeys(false);
		if(keys.size() >= 1)
		{
			for(String key : keys) 
			{
				pos1 = locations.getString("locations." + key + ".pos1");
				pos2 = locations.getString("locations." + key + ".pos2");
				config_world =  locations.getString("locations." + key + ".world");
				target_world = BlockLocation.getWorld().getName();
				if(!pos1.equals("") || !pos2.equals(""))
				{
					if(config_world.equalsIgnoreCase(target_world))
					{
						if(LibMain.IsWithin(BlockLocation, pos1, pos2)) 
						{
							re = key;
							break;
						}
					}
				}
			}
		}

		
		return re;
	}
	
	/*
	 * 호출되면 locations.yml에 지정되있는 모든 포인트의 블럭을 초기화 하는 코드를 작성.
	 * 각 반복문은 locations.yml의 각 항목의 initial_ore 항목을 사용하여 config에서 값을
	 * 얻어와서 해당 값으로 해당 좌표를 초기화
	*/
	public void InitializeMine() 
	{
		// 해당하는 월드의 이름을 담을 버퍼를 생성
		String worldname = "";
				
		// 시작 좌표를 저장하기위한 버퍼를 생성
		String pos1 = "";
		
		// 끝 좌표를 저장하기 위한 버퍼를 생성
		String pos2 = "";
		
		// 초기화할 블럭의 정보를 담을 버퍼를 생성
		String Initial_key = "";
		
		// 각각의 좌표를 담을 변수를와 임시변수 생성
		int x1,y1,z1,x2,y2,z2,temp=0;
		
		// 초기화할 블럭의 정보를 담기위한 변수를 선언
		int id = 0;
		byte meta = 0;
		
		// 각 값을 얻어오기위해 키 집합 생성
		Set<String> keys = locations.getConfigurationSection("locations").getKeys(false);
		
		// locations.yml의 모든 항목에 접근하기위해 반복문 사용
		for(String key : keys) 
		{
			// key 항목의 시작지점을 pos1에 저장
			pos1 = locations.getString("locations." + key + ".pos1");
			
			// key 항목의 끝 지점을 pos2에 저장
			pos2 = locations.getString("locations." + key + ".pos2");
			
			// 좌표가 존재하는 월드 이름을 worldname에 저장
			worldname = locations.getString("locations." + key + ".world");
			
			// 초기화할 블럭의 config 좌표를 저장함.
			Initial_key = locations.getString("locations." + key + ".initial_ore");
			
			// 초기화할 블럭의 id를 얻어옴
			id = c.getInt("config." + Initial_key + ".id");
			
			// 초기화할 블럭의 메타데이타를 얻어와 byte 형으로 형변환
			meta = (byte)c.getInt("config." + Initial_key + ".meta");
			
			// 각 좌표값을 pos1, pos2로부터 얻어냄
			String[] split1 = pos1.split(",");
			String[] split2 = pos2.split(",");
			x1 = Integer.parseInt(split1[0]);
			y1 = Integer.parseInt(split1[1]);
			z1 = Integer.parseInt(split1[2]);
			x2 = Integer.parseInt(split2[0]);
			y2 = Integer.parseInt(split2[1]);
			z2 = Integer.parseInt(split2[2]);
			
			// 좌표 값들의 순서가 바뀌어있을 경우 올바르게 변경
			if(x1 > x2) 
			{
				temp = x1;
				x1 = x2;
				x2 = temp;
			}
			if(y1 > y2) 
			{
				temp = y1;
				y1 = y2;
				y2 = temp;
			}
			if(z1 > z2)
			{
				temp = z1;
				z1 = z2;
				z2 = temp;
			}
			// 광산으로 지정된 모든 x축에 대해서 반복
			for(int x = x1 ; x <= x2 ; x++)
			{
				// 광산으로 지정된 모든 y축에 대해서 반복
				for(int y = y1 ; y <= y2 ; y++) 
				{
					// 광산으로 지정된 모든 z 축에 대해서 반복
					for(int z = z1 ; z <= z2 ; z++) 
					{
						// 블럭을 생성하기위한 Location의 인스턴스를 제작
						Location newloc = new Location(Bukkit.getWorld(worldname), (double)x, (double)y, (double)z);
						
						// Location 인스턴스를 기반으로 해당 위치에 맞는 블럭을 생성
						// 블럭 피직스를 호출하지 않기 위해 마지막 인수를 false로 설정
						Bukkit.getWorld(worldname).getBlockAt(newloc).setTypeIdAndData(id, meta, false);
					}
				}
			}
		}
	}

}
