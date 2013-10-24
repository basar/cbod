package net.bsrc.cbod.core.persistence;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.core.util.ConfigurationUtil;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;

/**
 * User: bsr Date: 10/20/13 Time: 4:57 PM
 */
public class DB4O {

	private static DB4O instance = null;

	private ObjectContainer objContainer;

	private String db4oFilePath;

	private DB4O() {

	}

	public static DB4O getInstance() {
		if (instance == null) {
			synchronized (DB4O.class) {
				if (instance == null) {
					instance = new DB4O();
					instance.initialize();
				}
			}
		}
		return instance;
	}

	private void initialize() {

		String temp = ConfigurationUtil.getString(CBODConstants.DB4O_FILE_PATH);
		db4oFilePath = CBODUtil.getDefaultOutputDirectoryPath().concat(temp);
		objContainer = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(),
				db4oFilePath);
	}

	public ObjectContainer getObjContainer() {
		return objContainer;
	}

	public void close() {
		if (objContainer != null) {
			objContainer.close();
		}
	}
}
