import cmd.CmdOptions;
import cmd.GetOptions;
import ziptrack.zip.core.ZipEngine;

// Analyzes compressed traces for HB races using a vector clock algorithm.
public class ZipVC {
	public static void main(String args[]){
		CmdOptions options = new GetOptions(args).parse();
		ZipEngine.analyze(options.map_file, options.trace_file, false);
	}
}
