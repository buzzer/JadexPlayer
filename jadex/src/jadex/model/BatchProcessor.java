package jadex.model;

import java.io.*;
import java.util.*;

import jadex.util.collection.SCollection;
import jadex.util.*;
import jadex.config.Configuration;


/**
 *  Batch-process models (e.g., for checking).
 */
public class BatchProcessor
{
	//-------- attributes --------

	/** Print out each processed file. */
	protected boolean verbose;

	/** Parse also recursively subcapabilities from one model. */
	protected boolean recursive_parse;

	/** Find models also recursively in directories. */
	protected boolean recursive_dirsearch;

	//-------- constrcutors --------

	/**
	 *  Create a new batch processor.
	 *  @param v Verbose output for each file.
	 *  @param rp Compile also all capabilities contained in a model.
	 *  @param rd Search recusively in subdirectories.
	 */
	public BatchProcessor(boolean v, boolean rp, boolean rd)
	{
		this.verbose = v;
		this.recursive_parse = rp;
		this.recursive_dirsearch = rd;
	}

	//-------- methods --------

	/**
	 *  Process a list of files and direcctories.
	 *  @param fileinfos The list of file infos.
	 */
	public void processFiles(FileInfo[] fileinfos)
	{
		long start = System.currentTimeMillis();
		
		List	files	= new ArrayList(Arrays.asList(fileinfos));
		
		int model_cnt = 0;
		int error_cnt = 0;
		for(int i=0; i<files.size(); i++)
		{
			try
			{
				FileInfo fi = (FileInfo)files.get(i);
				
				// Recurse for directories.
				if(fi instanceof DirectoryInfo)
				{
					ResourceInfo rinfo = SUtil.getResourceInfo0(fi.filename);
					files.addAll(findModelsInDir(rinfo.getFilename()));
					rinfo.cleanup();
				}
								
				// Process an agent model.
				else if(fi instanceof AgentInfo)
				{
					if(verbose)
						System.out.println("Processing "+fi.filename);
					model_cnt++;
					IMBDIAgent	agent	= SXML.loadAgentModel(fi.filename, null);
					error_cnt	+= processAgentModel(agent);
					
					// Add included properties and subcapabilities.
					if(recursive_parse)
					{
						if(agent.getPropertyFile()!=null)
						{
							PropertiesInfo	pi	= new PropertiesInfo(agent.getPropertyFile(), agent.getFullImports());
							if(!files.contains(pi))
							{
								files.add(pi);
							}
						}
						IMCapabilityReference[] caprefs = agent.getCapabilityReferences();
						for(int j=0; j<caprefs.length; j++)
						{
							CapabilityInfo	ci	= new CapabilityInfo(caprefs[j].getFile(), agent.getFullImports());
							if(!files.contains(ci))
							{
								files.add(ci);
							}
						}
					}
				}
				
				// Process a capability model.
				else if(fi instanceof CapabilityInfo)
				{
					if(verbose)
						System.out.println("Processing "+fi.filename);
					model_cnt++;
					IMCapability	capa	= SXML.loadCapabilityModel(fi.filename, null, null);
					error_cnt	+= processCapabilityModel(capa);
					
					// Add included subcapabilities.
					if(recursive_parse)
					{
						IMCapabilityReference[] caprefs = capa.getCapabilityReferences();
						for(int j=0; j<caprefs.length; j++)
						{
							CapabilityInfo	ci	= new CapabilityInfo(caprefs[j].getFile(), capa.getFullImports());
							if(!files.contains(ci))
							{
								files.add(ci);
							}
						}
					}
				}
				
				// Process a properties model.
				else if(fi instanceof PropertiesInfo)
				{
					if(verbose)
						System.out.println("Processing "+fi.filename);
					model_cnt++;
					IMPropertybase	props	= SXML.loadPropertiesModel(fi.filename, null, null);
					error_cnt	+= processPropertiesModel(props);
				}
			}
			catch(Throwable e)
			{
				error_cnt++;
				System.err.println("Error in processing: "+((FileInfo)files.get(i)).filename);
				e.printStackTrace();
			}
		}

		long end = System.currentTimeMillis();
		int secs = (int)(end-start)/1000;

		System.out.println("Processed "+model_cnt+" models in "+secs+" seconds.");
		System.out.println("Found "+error_cnt+" errors.");
	}

	/**
	 *  Process the given properties model.
	 *  Performs an integrity check.
	 *  Override for different processing behaviour. 
	 */
	protected int	processPropertiesModel(IMPropertybase props)
	{
		// Todo: Fails because of no scope (-> no parser).
//		Report	report	= props.getReport();
//		if(!report.isEmpty())
//		{
//			System.out.println(report.toString());
//		}
		return 0;
	}

	/**
	 *  Process the given capability model.
	 *  Performs an integrity check.
	 *  Override for different processing behaviour. 
	 */
	protected int	processCapabilityModel(IMCapability capa)
	{
		IReport	report	= capa.getReport();
		if(!report.isEmpty())
		{
			System.out.println(report.toString());
		}
		return report.getErrorCount();
	}

	/**
	 *  Process the given agent model.
	 *  Performs an integrity check.
	 *  Override for different processing behaviour. 
	 */
	protected int	processAgentModel(IMBDIAgent agent)
	{
		IReport	report	= agent.getReport();
		if(!report.isEmpty())
		{
			System.out.println(report.toString());
		}
		return report.getErrorCount();
	}

	/**
	 *  Find all models in a directory.
	 *  If rec_dirsearch is on, subdirectories will also be reported.
	 *  @return A list of all models and directories to parse.
	 *  @throws IOException 
	 */
	protected List findModelsInDir(String dirname) throws IOException
	{
		assert dirname!=null;
		List ret = SCollection.createArrayList();

		File dir = new File(dirname);
		if(!dir.isDirectory())
			throw new RuntimeException("Must be directory: "+dirname);

		// Find capabilities.
		File[] capas = dir.listFiles(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return SXML.isCapabilityFilename(name);
			}
		});
		for(int i=0; i<capas.length; i++)
		{
			ret.add(new CapabilityInfo(capas[i].getAbsolutePath(), null));
		}

		// Find agents.
		File[] agents = dir.listFiles(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return SXML.isAgentFilename(name);
			}
		});
		for(int i=0; i<agents.length; i++)
		{
			ret.add(new AgentInfo(agents[i].getAbsolutePath(), null));
		}

		// Find properties.
		File[] properties = dir.listFiles(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return SXML.isPropertiesFilename(name);
			}
		});
		for(int i=0; i<properties.length; i++)
		{
			ret.add(new PropertiesInfo(properties[i].getAbsolutePath(), null));
		}

		// Search in subdirectories.
		if(recursive_dirsearch)
		{
			File[] subdirs = dir.listFiles(new FileFilter()
			{
				public boolean accept(File pathname)
				{
					return pathname.isDirectory();
				}
			});
			for(int i=0; i<subdirs.length; i++)
			{
				ret.add(new DirectoryInfo(subdirs[i].getAbsolutePath()));
			}
		}
		return ret;
	}

	/**
	 *  Print out the usage.
	 */
	public static void printUsage()
	{
		System.out.println("Usage: java jadex.model.BatchProcessor [-v] [-rp] [-rd] (files)+\n");
		System.out.println("-v: Verbose output printing each processed file.");
		System.out.println("-rp: Turn on recusive processing, i.e. process contained capabilities also");
		System.out.println("-rd: Turn on recusive directory search, i.e. process models contained in subdirectories also");
		System.out.println("(files)+: one or more directories, or agent (.agent.xml), capability (.capability.xml), or properties (.properties.xml) files.");
	}

	/**
	 *  Main for invoking the batch processor.
	 *
	 *  Usage:
	 *  java jadex.model.BatchProcessor [-rd] [-rp] (files)*
	 *
	 *  Examples:
	 *  java jadex.model.BatchProcessor -rd "."
	 *  @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{
		Configuration.setFallbackConfiguration("jadex/config/batch_conf.properties");

		boolean v = false;
		boolean rp = false;
		boolean rd = false;
		List files = SCollection.createArrayList();

		for(int i=0; i<args.length; i++)
		{
			if(args[i].equals("-v"))
			{
				v = true;
			}
			else if(args[i].equals("-rp"))
			{
				rp = true;
			}
			else if(args[i].equals("-rd"))
			{
				rd = true;
			}
			else
			{
				if(args[i].endsWith("capability.xml"))
					files.add(new CapabilityInfo(args[i], null));
				else if(args[i].endsWith("agent.xml"))
					files.add(new AgentInfo(args[i], null));
				else if(args[i].endsWith("properties.xml"))
					files.add(new PropertiesInfo(args[i], null));
				else
					files.add(new DirectoryInfo(args[i]));
			}
		}

		if(files.size()==0)
		{
			printUsage();
			return;
		}

		BatchProcessor prec = new BatchProcessor(v, rp, rd);
		prec.processFiles((FileInfo[])files.toArray(new FileInfo[files.size()]));
	}

	//-------- helper classes --------

	/**
	 *  Struct for saving file info.
	 */
	public abstract static class FileInfo
	{
		public String filename;

		/**
		 *  Create a new file info.
		 *  @param filename The absolute filename.
		 */
		public FileInfo(String filename)
		{
			this.filename = filename;
		}

		/**
		 *  Check if two file infos are equal
		 *  (i.e. pointing to the same file).
		 */
		public boolean equals(Object obj)
		{
			return obj instanceof FileInfo && ((FileInfo)obj).filename.equals(filename);
		}
		
		/**
		 *  Generate a hash code for this object.
		 */
		public int hashCode()
		{
			return filename.hashCode();
		}
	}

	/**
	 *  Struct for saving directory info.
	 */
	public static class DirectoryInfo extends FileInfo
	{
		/**
		 *  Create a new file info.
		 *  @param filename The filename.
		 */
		public DirectoryInfo(String filename)
		{
			super(filename);
		}
	}

	/**
	 *  Struct for saving file info for capabilities.
	 */
	public static class CapabilityInfo extends FileInfo
	{
		/**
		 *  Create a new capability info.
		 *  @param filename The absolute or relative filename.
		 *  @param imports The imports for loading.
		 *  @throws IOException 
		 */
		public CapabilityInfo(String filename, String[] imports) throws IOException
		{
			super(SXML.getResourceInfo(filename, SXML.FILE_EXTENSION_CAPABILITY, imports).getFilename());
		}
	}

	/**
	 *  Struct for saving file info for properties.
	 */
	public static class PropertiesInfo extends FileInfo
	{
		/**
		 *  Create a new properties info.
		 *  @param filename The absolute or relative filename.
		 *  @param imports The imports for loading.
		 *  @throws IOException 
		 */
		public PropertiesInfo(String filename, String[] imports) throws IOException
		{
			super(SXML.getResourceInfo(filename, SXML.FILE_EXTENSION_PROPERTIES, imports).getFilename());
		}
	}

	/**
	 *  Struct for saving file info for agents.
	 */
	public static class AgentInfo extends FileInfo
	{
		/**
		 *  Create a new agent info.
		 *  @param filename The absolute or relative filename.
		 *  @param imports The imports for loading.
		 *  @throws IOException 
		 */
		public AgentInfo(String filename, String[] imports) throws IOException
		{
			super(SXML.getResourceInfo(filename, SXML.FILE_EXTENSION_AGENT, imports).getFilename());
		}
	}
}
