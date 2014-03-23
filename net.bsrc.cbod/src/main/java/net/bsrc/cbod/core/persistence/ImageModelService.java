package net.bsrc.cbod.core.persistence;

import java.util.List;

import net.bsrc.cbod.core.model.Descriptor;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.EObjectType;
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

		ObjectContainer container = db4O.getObjContainer();

		try {
			container.store(imageModel);
		} catch (Exception ex) {
			container.rollback();
			logger.error("", ex);
		}
	}

	public void saveImageModelList(List<ImageModel> imageModelList) {

		if (!CollectionUtils.isEmpty(imageModelList)) {

			ObjectContainer container = db4O.getObjContainer();
			try {
				for (ImageModel imgModel : imageModelList) {
					container.store(imgModel);
				}
			} catch (Exception ex) {
				container.rollback();
				logger.error("", ex);
			}

		}
	}



	public List<ImageModel> getImageModelList(final boolean isTestImage,
			final EObjectType objectType) {

		ObjectContainer container = db4O.getObjContainer();

		@SuppressWarnings("serial")
		List<ImageModel> imageModels = container
				.query(new Predicate<ImageModel>() {
					@Override
					public boolean match(ImageModel imageModel) {
						return imageModel.isTestImage() == isTestImage
								&& imageModel.getObjectType() == objectType;

					}
				});

		return imageModels;
	}

	public List<ImageModel> getImageModelList(final EObjectType objectType) {
		
		ObjectContainer container = db4O.getObjContainer();

		@SuppressWarnings("serial")
		List<ImageModel> imageModels = container
				.query(new Predicate<ImageModel>() {
					@Override
					public boolean match(ImageModel imageModel) {
						return imageModel.getObjectType() == objectType;

					}
				});

		return imageModels;
	}

	
	private boolean controlDescriptors(ImageModel imgModel) {

		boolean passed = true;

		for (EDescriptorType type : EDescriptorType.values()) {
			// TODO Gecici yapildi
			if (type != EDescriptorType.HTD) {
				Descriptor descriptor = imgModel.getDescriptor(type);
				if (descriptor != null
						&& !CollectionUtils.isEmpty(descriptor.getDataList())) {
					continue;
				} else {
					passed = false;
					break;
				}
			}

		}

		return passed;
	}
}
