package net.bsrc.cbod.core.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.bsrc.cbod.core.exception.CBODException;
import net.bsrc.cbod.core.model.Descriptor;
import net.bsrc.cbod.core.model.EDataType;
import net.bsrc.cbod.core.model.EDescriptorType;
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
					if (controlDescriptors(imgModel))
						container.store(imgModel);
				}
			} catch (Exception ex) {
				container.rollback();
				logger.error("", ex);
			}

		}
	}

	public List<ImageModel> getImageModelList(final EDataType dataType,
			final boolean isNegativeImage) {

		ObjectContainer container = db4O.getObjContainer();

		List<ImageModel> imageModels = container
				.query(new Predicate<ImageModel>() {
					@Override
					public boolean match(ImageModel imageModel) {
						return imageModel.isNegativeImg() == isNegativeImage
								&& imageModel.getDataType() == dataType;
					}
				});

		return imageModels;
	}

	public List<ImageModel> getImageModelList(final EDataType dataType,
			final String partName) {

		ObjectContainer container = db4O.getObjContainer();

		List<ImageModel> imageModels = container
				.query(new Predicate<ImageModel>() {
					@Override
					public boolean match(ImageModel imageModel) {
						return imageModel.getDataType() == dataType
								&& imageModel.getObjectPart().equals(partName);

					}
				});

		return imageModels;
	}

	public List<ImageModel> getImageModelList(final String partName) {
		ObjectContainer container = db4O.getObjContainer();

		List<ImageModel> imageModels = container
				.query(new Predicate<ImageModel>() {
					@Override
					public boolean match(ImageModel imageModel) {
						return imageModel.getObjectPart().equals(partName);

					}
				});

		return imageModels;
	}

	private List<ImageModel> getNegativeImageModelList(
			final EDataType dataType, int amount, boolean randomize) {

		// TODO amount sadece randomize parametresi true olarak geldiginde
		// kullaniliyor. Burasi duzenlenmeli!
		if (amount > 0 && !randomize) {
			throw new CBODException("Hatali parametre");
		}

		ObjectContainer container = db4O.getObjContainer();

		List<ImageModel> imageModels = container
				.query(new Predicate<ImageModel>() {
					@Override
					public boolean match(ImageModel imageModel) {
						if (dataType == null) {
							return true;
						}
						return imageModel.getDataType() == dataType
								&& imageModel.isNegativeImg();

					}
				});

		if (randomize) {

			List<ImageModel> result = new ArrayList<ImageModel>();
			amount = Math.min(imageModels.size(), amount);

			int tmp = imageModels.size() - 1;

			Random rnd = new Random();

			while (result.size() < amount) {
				result.add(imageModels.get(rnd.nextInt(tmp)));
			}

			imageModels = result;
		}

		return imageModels;

	}

	public List<ImageModel> getNegativeImageModelList(final EDataType dataType,
			int amount) {

		return getNegativeImageModelList(dataType, amount, false);
	}

	public List<ImageModel> getRandomNegativeImageModeList(
			final EDataType dataType, int amount) {
		return getNegativeImageModelList(dataType, amount, true);
	}

	public List<ImageModel> getRandomNegativeImageModelList(int amount) {
		return getNegativeImageModelList(null, amount, true);
	}

	public List<ImageModel> getNegativeImageModelList() {
		return getNegativeImageModelList(null, 0, false);
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
