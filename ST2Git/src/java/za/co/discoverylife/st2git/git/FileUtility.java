/*****************************************************************************
    This file is part of Git-Starteam.

    Git-Starteam is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Git-Starteam is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Git-Starteam.  If not, see <http://www.gnu.org/licenses/>.
******************************************************************************/
package za.co.discoverylife.st2git.git;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public class FileUtility {
	
	public static void rmDir(File dir) throws IOException {
		if(null != dir && dir.exists()) {
			for(File f : dir.listFiles()) {
				if(f.isDirectory()) {
					rmDir(f);
				} else {
					f.delete();
				}
			}
			dir.delete();
		}
	}
	
	public static void close(Closeable... toClose) {
		for(Closeable c : toClose) {
			if(null != c) {
				try {
					c.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
