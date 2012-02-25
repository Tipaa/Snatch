package com.mojang.mojam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.mojang.mojam.entity.Entity;
import com.mojang.mojam.entity.mob.Bat;
import com.mojang.mojam.entity.mob.Mummy;
import com.mojang.mojam.entity.mob.Snake;
import com.mojang.mojam.level.Level;
import com.mojang.mojam.network.Packet;

public final class Snatch
{
	private static boolean init = false;
	public static File modDir;
	public static List<Mod> modList = new ArrayList<Mod>();
	private static MojamComponent mojam;
	public static Map<Integer, Entity> spawnList = new HashMap<Integer, Entity>();

	public static void init(MojamComponent m)
	{
		init = true;
		mojam = m;
		try
		{
			modDir = new File(Snatch.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		}
		catch (URISyntaxException e1)
		{
			e1.printStackTrace();
		}
		System.out.println("init");
		try
		{
			System.out.println(modDir.getAbsolutePath());
			readFromClassPath(modDir);
			//readFromModFolder(modDir);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		for(Mod mod : modList)
		{
			//System.out.println(mod.getClass());
		}
	}

	private static void readFromModFolder(File file) throws IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException
	{
		ClassLoader classloader = (com.mojang.mojam.MojamComponent.class).getClassLoader();
		Method method = (java.net.URLClassLoader.class).getDeclaredMethod("addURL", new Class[]
		{
			java.net.URL.class
		});
		method.setAccessible(true);
		if(!file.isDirectory())
		{
			throw new IllegalArgumentException("Folder must be a Directory.");
		}
		File afile[] = file.listFiles();
		Arrays.sort(afile);
		if(classloader instanceof URLClassLoader)
		{
			for(int i = 0; i < afile.length; i++)
			{
				File file1 = afile[i];
				if(file1.isDirectory() || file1.isFile() && (file1.getName().endsWith(".jar") || file1.getName().endsWith(".zip")))
				{
					method.invoke(classloader, new Object[]
					{
						file1.toURI().toURL()
					});
				}
			}
		}
		for(int j = 0; j < afile.length; j++)
		{
			File file2 = afile[j];
			if(file2.isDirectory() || file2.isFile() && (file2.getName().endsWith(".jar") || file2.getName().endsWith(".zip")))
			{
				if(file2.isFile())
				{
					FileInputStream fileinputstream = new FileInputStream(file2);
					ZipInputStream zipinputstream = new ZipInputStream(fileinputstream);
					Object obj = null;
					do
					{
						ZipEntry zipentry = zipinputstream.getNextEntry();
						if(zipentry == null)
						{
							break;
						}
						String s1 = zipentry.getName();
						if(!zipentry.isDirectory() && s1.startsWith("mod_") && s1.endsWith(".class"))
						{
							addMod(classloader, s1);
						}
					}
					while(true);
					zipinputstream.close();
					fileinputstream.close();
				}
				else if(file2.isDirectory())
				{
					Package package1 = (com.mojang.mojam.MojamComponent.class).getPackage();
					if(package1 != null)
					{
						String s = package1.getName().replace('.', File.separatorChar);
						file2 = new File(file2, s);
					}
					File afile1[] = file2.listFiles();
					if(afile1 != null)
					{
						for(int k = 0; k < afile1.length; k++)
						{
							String s2 = afile1[k].getName();
							if(afile1[k].isFile() && s2.startsWith("mod_") && s2.endsWith(".class"))
							{
								addMod(classloader, s2);
							}
						}
					}
				}
			}
		}
	}
	
	public static MojamComponent getMojam()
	{
		return mojam;
	}

	private static void addMod(ClassLoader classloader, String s)
	{
		try
		{
			String s1 = s.split("\\.")[0];
			Package package1 = (com.mojang.mojam.Snatch.class).getPackage();
			if(package1 != null)
			{
				s1 = (new StringBuilder(String.valueOf(package1.getName()))).append(".").append(s1.substring(s.lastIndexOf('/')+1)).toString();
			}
			Class class1 = classloader.loadClass(s1);
			if(!(Mod.class).isAssignableFrom(class1))
			{
				return;
			}
			Mod mod = (Mod) class1.newInstance();
			if(mod != null)
			{
				modList.add(mod);
				System.out.println((new StringBuilder("Mod Initialized: ")).append(mod.toString()).toString());
			}
		}
		catch (Exception throwable)
		{
			throwable.printStackTrace();
			System.out.println((new StringBuilder("Failed to load mod from \"")).append(s).append("\"").toString());
		}
	}

	private static void readFromClassPath(File file) throws FileNotFoundException, IOException
	{
		ClassLoader classloader = (Snatch.class).getClassLoader();
		if(file.isFile() && (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")))
		{
			System.out.println("Reading from classpath "+file.getAbsolutePath());
			FileInputStream fileinputstream = new FileInputStream(file);
			ZipInputStream zipinputstream = new ZipInputStream(fileinputstream);
			Object obj = null;
			do
			{
				ZipEntry zipentry = zipinputstream.getNextEntry();
				if(zipentry == null)
				{
					break;
				}
				String s1 = zipentry.getName();
				if(!zipentry.isDirectory() && s1.contains("mod_") && s1.endsWith(".class"))
				{
					System.out.println(s1);
					addMod(classloader, s1);
				}
			}
			while(true);
			fileinputstream.close();
		}
		else if(file.isDirectory())
		{
			try
			{
				readFromModFolder(file);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			Package package1 = (Snatch.class).getPackage();
			if(package1 != null)
			{
				String s = package1.getName().replace('.', File.separatorChar);
				file = new File(file, s);
			}
			File afile[] = file.listFiles();
			if(afile != null)
			{
				for(int i = 0; i < afile.length; i++)
				{
					String s2 = afile[i].getName();
					if(afile[i].isFile() && s2.startsWith("mod_") && s2.endsWith(".class"))
					{
						addMod(classloader, s2);
					}
				}
			}
		}
	}

	public static Entity getEntityById(int i, double x, double y)
	{
		for(Mod m : modList)
		{
			Entity e = m.getEntityInstanceById(i, x, y);
			//System.out.println("Entity Id: "+i+" in class "+m.getClass()+" created "+e);
			if(e != null) return e;
		}
		return null;
	}
	
	public static int numEntitiesLoaded()
	{
		return spawnList.size();
	}

	public static int addEntity(Entity entity)
	{
		spawnList.put(spawnList.size(), entity);
		for(int i = 0; i<spawnList.size();i++)
		{
			System.out.println("Registered"+spawnList.get(i));
		}
		return spawnList.size()-1;
	}

	public static void afterRender()
	{
		for(Mod m : modList)
		{
			m.OnRender();
		}
	}

	public static void startRender()
	{
		for(Mod m : modList)
		{
			m.OnStartRender();
		}
	}

	public static void afterTick()
	{
		for(Mod m : modList)
		{
			m.AfterTick();
		}
	}

	public static void runOnce()
	{
		for(Mod m : modList)
		{
			m.RunOnce();
		}
	}

	public static void createLevel(Level level)
	{
		for(Mod m : modList)
		{
			m.CreateLevel(level);
		}
	}

	public static void onStop()
	{
		for(Mod m : modList)
		{
			m.OnClose();
		}
	}

	public static void onWin(int i)
	{
		for(Mod m : modList)
		{
			m.OnVictory(i);
		}
	}

	public static void levelTick(Level level)
	{
		for(Mod m : modList)
		{
			m.OnLevelTick(level);
		}
	}

	public static void updateTick()
	{
		for(Mod m : modList)
		{
			m.OnTick();
		}
	}

	public static void sendPacket(Packet packet)
	{
		for(Mod m : modList)
		{
			m.OnSendPacket(packet);
		}
	}

	public static void receivePacket(Packet packet)
	{
		for(Mod m : modList)
		{
			m.OnReceivePacket(packet);
		}
	}

}
