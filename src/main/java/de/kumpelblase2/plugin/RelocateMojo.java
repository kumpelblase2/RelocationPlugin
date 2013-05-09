package de.kumpelblase2.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 *	This is a plugin to replace strings in all source files, preferable changing imports.
 *	@goal relocate
 */
@Mojo( name = "relocation", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class RelocateMojo extends AbstractMojo
{
    /**
     * The output directory for the changed files
     */
    @Parameter( property = "outputDir", required = true, defaultValue = "${project.build.sourceDirectory}" )
    private File outputDirectory;
    
    /**
     * The original directory of the source files
     */
    @Parameter(property = "source", required = true)
    private File sourceDir;
    
    /**
     * Properties for this plugin
     */
    @Parameter(property = "properties", required = true)
    private Properties properties; 
    
    private int filesSearched = 0;
    private Map<String, String> replaces = new HashMap<String, String>();

    public void execute()
    {
    	for(Entry<Object, Object> values : this.properties.entrySet())
    	{
    		this.replaces.put((String)values.getKey(), (String)values.getValue());
    	}
    	
    	this.outputDirectory.delete();
    	this.outputDirectory.mkdir();
    	this.getLog().info("replaces: " + this.replaces.size());
    	this.getLog().info("source: " + this.sourceDir.getAbsolutePath());
    	this.getLog().info("target: " + this.outputDirectory.getAbsolutePath());
    	this.changeFiles(this.sourceDir);
    	this.getLog().info("Checked " + this.filesSearched + " files.");
    }
    
    public void changeFiles(File inDir)
    {
    	for(File f : inDir.listFiles())
    	{
    		if(f.isDirectory())
    		{
    			changeFiles(f);
    			continue;
    		}
    		
    		try{
				BufferedReader reader = new BufferedReader(new FileReader(f));
				List<String> lines = new ArrayList<String>();
				String line;
				while((line = reader.readLine()) != null)
				{
					for(Entry<String, String> rename : this.replaces.entrySet())
					{
						line = line.replace(rename.getKey(), rename.getValue());
					}
					lines.add(line);
				}
				reader.close();
				
				File f2 = new File(this.outputDirectory, f.getAbsolutePath().replace(this.sourceDir.getAbsolutePath(), ""));
				f2.getParentFile().mkdirs();
				f2.createNewFile();
				FileWriter writer = new FileWriter(f2);
				for(String writeline : lines)
				{
					writer.write(writeline + "\n");
				}
				writer.close();
			}catch (Exception e){
				e.printStackTrace();
			}
    		this.filesSearched++;
    	}
    }
}
