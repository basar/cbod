package net.bsrc.cbod.core.persistence;

import java.util.List;

import net.bsrc.cbod.core.model.EDataType;
import net.bsrc.cbod.core.model.ImageModel;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db4o.ObjectContainer;
import com.db4o.query.Predicate;

/**
 * User: bsr Date: 10/20/13 Time: 5:03 PM
 */
public class ImageModelService {

	private final static Logger logger = LoggerFactory
			.getLogger(ImageModelService.class);

	private static ImageModelService instance = null;

	private DB4O db4O;

	private ImageModelService() {

	}

	public static ImageModelService getInstance() {
		if (instance == null) {
			synchronized (ImageModelService.class) {
				if (instance == null) {
					instance = new ImageModelService();
					instance.initialize();

				}
			}
		}
		return instance;
	}

	private void initialize() {
		db4O = DB4O.getInstance();
	}

	public void saveImageModel(ImageModel imageModel) {

		ObjectContainer db = db4O.getObjContainer();

		try {
			db.store(imageModel);
		} catch (Exception ex) {
			db.rollback();
			logger.error("", ex);
		}
	}

	public void saveImageModelList(List<ImageModel> imageModelList) {

		if (!CollectionUtils.isEmpty(imageModelList)) {

			ObjectContainer db = db4O.getObjContainer();
			try {
				for (ImageModel imgModel : imageModelList) {
					db.store(imgModel);
				}
			} catch (Exception ex) {
				db.rollback();
				logger.error("", ex);
			}

		}
	}

	public List<ImageModel> getImageModelList(final EDataType dataType,
			final boolean isNegativeImage) {

		ObjectContainer db = db4O.getObjContainer();

		List<ImageModel> imageModels = db.query(new Predicate<ImageModel>() {
			@Override
			public boolean match(ImageModel imageModel) {
				return imageModel.isNegativeImg() == isNegativeImage
						&& imageModel.getDataType() == dataType;
			}
		});

		return imageModels;
	}
}