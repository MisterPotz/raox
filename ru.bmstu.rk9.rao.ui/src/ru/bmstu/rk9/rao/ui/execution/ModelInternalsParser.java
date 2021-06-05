package ru.bmstu.rk9.rao.ui.execution;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.eclipse.xtext.util.ReflectionUtil;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.typesystem.IBatchTypeResolver;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;

import ru.bmstu.rk9.rao.jvmmodel.GeneratedCodeContract;
import ru.bmstu.rk9.rao.lib.animation.AnimationFrame;
import ru.bmstu.rk9.rao.lib.database.Database.DataType;
import ru.bmstu.rk9.rao.lib.dpt.AbstractDecisionPoint;
import ru.bmstu.rk9.rao.lib.dpt.Logic;
import ru.bmstu.rk9.rao.lib.dpt.Search;
import ru.bmstu.rk9.rao.lib.exception.RaoLibException;
import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.modeldata.ModelStructureConstants;
import ru.bmstu.rk9.rao.lib.naming.NamingHelper;
import ru.bmstu.rk9.rao.lib.pattern.Pattern;
import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.lib.resource.ComparableResource;
import ru.bmstu.rk9.rao.lib.result.AbstractResult;
import ru.bmstu.rk9.rao.lib.simulator.ReflectionUtils;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorInitializationInfo;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorPreinitializationInfo;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorCommonModelInfo;
import ru.bmstu.rk9.rao.lib.varconst.VarConst;
import ru.bmstu.rk9.rao.rao.PatternType;
import ru.bmstu.rk9.rao.rao.RaoEntity;
import ru.bmstu.rk9.rao.rao.RaoModel;
import ru.bmstu.rk9.rao.rao.RelevantResource;
import ru.bmstu.rk9.rao.rao.RelevantResourceTuple;
import ru.bmstu.rk9.rao.ui.gef.process.BlockConverter;
import ru.bmstu.rk9.rao.ui.gef.process.ProcessEditor;
import ru.bmstu.rk9.rao.ui.gef.process.model.ProcessModelNode;

@SuppressWarnings("restriction")
public class ModelInternalsParser {
	private final SimulatorPreinitializationInfo simulatorPreinitializationInfo = new SimulatorPreinitializationInfo();
	private final SimulatorInitializationInfo simulatorInitializationInfo = new SimulatorInitializationInfo();
	private final SimulatorCommonModelInfo simulatorCommonModelInfo = new SimulatorCommonModelInfo();

	private final List<Class<?>> decisionPointClasses = new ArrayList<>();

	private final List<VarConst> varconsts = new ArrayList<>();

	private final ModelContentsInfo modelContentsInfo = new ModelContentsInfo();

	private final List<Class<?>> animationClasses = new ArrayList<>();
	private final List<Class<?>> tupleClasses = new ArrayList<>();
	private final /* AnimationFrame */ List<Constructor<?>> animationFrames = new ArrayList<>();
	private final List<Field> resultFields = new ArrayList<>();

	private URLClassLoader classLoader;
	private final IProject project;
	private final IResourceSetProvider resourceSetProvider;
	private final IBatchTypeResolver typeResolver;

	public final SimulatorPreinitializationInfo getSimulatorPreinitializationInfo() {
		return simulatorPreinitializationInfo;
	}

	public final SimulatorInitializationInfo getSimulatorInitializationInfo() {
		return simulatorInitializationInfo;
	}

	public ModelInternalsParser(IProject project, IResourceSetProvider resourceSetProvider,
			IBatchTypeResolver typeResolver) {
		this.project = project;
		this.resourceSetProvider = resourceSetProvider;
		this.typeResolver = typeResolver;
	}

	public final void parse() throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException,
			MalformedURLException, CoreException {
		IProjectDescription description = project.getDescription();
		java.net.URI locationURI = description.getLocationURI();
		boolean useDefaultLocation = (locationURI == null);
		String location;

		if (useDefaultLocation)
			location = "file:///" + ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/"
					+ project.getName();
		else
			location = locationURI.toURL().toString();

		URL modelURL = new URL(location + "/bin/");

		URL[] urls = new URL[] { modelURL };

		classLoader = new URLClassLoader(urls, SimulatorWrapper.class.getClassLoader());

		simulatorPreinitializationInfo.modelStructure.put(ModelStructureConstants.NAME, project.getName());
		simulatorPreinitializationInfo.modelStructure.put(ModelStructureConstants.LOCATION,
				project.getLocation().toString());

		final ResourceSet resourceSet = resourceSetProvider.get(project);
		if (resourceSet == null) {
			System.out.println("resource set is null");
			return;
		}

		List<IResource> raoFiles = BuildUtil.getAllFilesInProject(project, "rao");
		simulatorPreinitializationInfo.modelStructure.put(ModelStructureConstants.NUMBER_OF_MODELS, raoFiles.size());

		for (IResource raoFile : raoFiles) {
			String raoFileName = raoFile.getName();
			raoFileName = raoFileName.substring(0, raoFileName.length() - ".rao".length());
			String modelClassName = project.getName() + "." + raoFileName;

			URI uri = BuildUtil.getURI(raoFile);
			org.eclipse.emf.ecore.resource.Resource modelResource = resourceSet.getResource(uri, true);
			if (modelResource == null) {
				System.out.println("model resource is null");
				continue;
			}

			EList<EObject> contents = modelResource.getContents();
			if (contents.isEmpty())
				continue;

			RaoModel model = (RaoModel) contents.get(0);

			parseModel(model, modelClassName);
		}
	}

	/* Nullable */
	private Class<?> findClassAndDo(List<Class<?>> classes, Predicate<Class<?>> predicate, Action action) {
		Optional<Class<?>> optionalClass = classes.stream().filter(predicate).findFirst();

		if (!optionalClass.isEmpty()) {
			if (action != null) {
				action.action(optionalClass.get());
			}
			return optionalClass.get();
		}
		return null;
	}

	private interface Action {
		void action(Class<?> clazz);
	}

	/**
	 * collects methods/classes that can change static state of model when called
	 * (but this method doesn't yet launch them) also collects serialization data
	 * about all fields/methods/classes (like resource types)
	 */
	@SuppressWarnings("unchecked")
	public final void parseModel(RaoModel model, String modelClassName)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Class<?> modelClass = Class.forName(modelClassName, false, classLoader);

		this.simulatorCommonModelInfo.setModelClass(modelClass);
		this.simulatorPreinitializationInfo.setSimulatorCommonModelInfo(simulatorCommonModelInfo);
		this.simulatorInitializationInfo.setSimulatorCommonModelInfo(simulatorCommonModelInfo);	

		// the static context of the model is available

		List<Class<?>> declaredClasses = Arrays.asList(modelClass.getDeclaredClasses());
		// need to check for initialization scope first
		Class<?> initializationScope = simulatorCommonModelInfo.getInitializationScopeClass();

		EList<RaoEntity> entities = model.getObjects();

		/** fill in the information about resource types */
		for (RaoEntity entity : entities) {
			if (!(entity instanceof ru.bmstu.rk9.rao.rao.ResourceType))
				continue;

			String name = modelClassName + "." + entity.getName();

			JSONArray parameters = new JSONArray();
			int offset = 0;
			int variableWidthParameterIndex = 0;
			for (ru.bmstu.rk9.rao.rao.FieldDeclaration field : ((ru.bmstu.rk9.rao.rao.ResourceType) entity)
					.getParameters()) {
				DataType dataType = DataType.getByName(field.getDeclaration().getParameterType().getSimpleName());

				JSONObject parameterJson = new JSONObject()
						.put(ModelStructureConstants.NAME, field.getDeclaration().getName())
						.put(ModelStructureConstants.TYPE, dataType).put(ModelStructureConstants.OFFSET, offset)
						.put(ModelStructureConstants.VARIABLE_WIDTH_PARAMETER_INDEX,
								dataType == DataType.OTHER ? variableWidthParameterIndex : -1);

				parameters.put(parameterJson);

				if (dataType == DataType.OTHER)
					variableWidthParameterIndex++;
				else
					offset += dataType.getSize();
			}

			JSONObject resourceJson = new JSONObject().put(ModelStructureConstants.NAME, name)
					.put(ModelStructureConstants.NAMED_RESOURCES, new JSONArray())
					.put(ModelStructureConstants.PARAMETERS, parameters)
					.put(ModelStructureConstants.FINAL_OFFSET, offset);

			simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.RESOURCE_TYPES)
					.put(resourceJson);
		}

		for (RaoEntity entity : entities) {
			String name = modelClassName + "." + entity.getName();

			/** fill in found events */
			if (entity instanceof ru.bmstu.rk9.rao.rao.VarConst) {
				simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.EVENTS)
				.put(new JSONObject().put(ModelStructureConstants.NAME, name));
				continue;
			}

			if (entity instanceof ru.bmstu.rk9.rao.rao.Event) {
				simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.EVENTS)
						.put(new JSONObject().put(ModelStructureConstants.NAME, name));
				continue;
			}

			if (entity instanceof ru.bmstu.rk9.rao.rao.Pattern) {
				String typeString = null;
				PatternType type = ((ru.bmstu.rk9.rao.rao.Pattern) entity).getType();

				switch (type) {
				case OPERATION:
					typeString = ModelStructureConstants.OPERATION;
					break;
				case RULE:
					typeString = ModelStructureConstants.RULE;
					break;
				}

				JSONArray relevantResources = new JSONArray();

				/** save java names of resources that are relevent to this pattern */
				for (RelevantResource relevant : ((ru.bmstu.rk9.rao.rao.Pattern) entity).getRelevantResources()) {
					LightweightTypeReference typeReference = typeResolver.resolveTypes(relevant.getValue())
							.getActualType(relevant.getValue());

					relevantResources.put(
							new JSONObject().put(ModelStructureConstants.NAME, name).put(ModelStructureConstants.TYPE,
									NamingHelper.changeDollarToDot(typeReference.getJavaIdentifier())));
				}

				/** same to tuples which contain more relevant resources */
				for (RelevantResourceTuple tuple : ((ru.bmstu.rk9.rao.rao.Pattern) entity).getRelevantTuples()) {
					for (JvmTypeReference tupleType : tuple.getTypes()) {
						relevantResources.put(new JSONObject().put(ModelStructureConstants.NAME, name).put(
								ModelStructureConstants.TYPE,
								NamingHelper.changeDollarToDot(tupleType.getIdentifier())));
					}
				}

				/** save patterns */
				simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.PATTERNS)
						.put(new JSONObject().put(ModelStructureConstants.NAME, name)
								.put(ModelStructureConstants.TYPE, typeString)
								.put(ModelStructureConstants.RELEVANT_RESOURCES, relevantResources));
				continue;
			}

			if (entity instanceof ru.bmstu.rk9.rao.rao.Logic) {
				JSONArray activities = new JSONArray();
				for (ru.bmstu.rk9.rao.rao.Activity activity : ((ru.bmstu.rk9.rao.rao.Logic) entity).getActivities())
					activities.put(new JSONObject().put(ModelStructureConstants.NAME, activity.getName()));

				simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.LOGICS)
						.put(new JSONObject().put(ModelStructureConstants.NAME, name)
								.put(ModelStructureConstants.ACTIVITIES, activities));
				continue;
			}

			if (entity instanceof ru.bmstu.rk9.rao.rao.Search) {
				JSONArray edges = new JSONArray();
				for (ru.bmstu.rk9.rao.rao.Edge edge : ((ru.bmstu.rk9.rao.rao.Search) entity).getEdges())
					edges.put(new JSONObject().put(ModelStructureConstants.NAME, edge.getName()));

				simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.SEARCHES)
						.put(new JSONObject().put(ModelStructureConstants.NAME, name).put(ModelStructureConstants.EDGES,
								edges));
				continue;
			}

			/**
			 * named resource is an instance of resource type find declared instances of
			 * each resource type and save them to their jsons (json of resource types)
			 */
			if (entity instanceof ru.bmstu.rk9.rao.rao.ResourceDeclaration) {
				XExpression constructor = ((ru.bmstu.rk9.rao.rao.ResourceDeclaration) entity).getConstructor();
				LightweightTypeReference typeReference = typeResolver.resolveTypes(constructor)
						.getActualType(constructor);
				String typeName = NamingHelper.changeDollarToDot(typeReference.getJavaIdentifier());

				JSONArray resourceTypes = simulatorPreinitializationInfo.modelStructure
						.getJSONArray(ModelStructureConstants.RESOURCE_TYPES);
				JSONObject resourceType = null;
				for (int i = 0; i < resourceTypes.length(); i++) {
					resourceType = resourceTypes.getJSONObject(i);
					if (resourceType.getString(ModelStructureConstants.NAME).equals(typeName))
						break;
				}

				if (resourceType == null)
					throw new RuntimeException("Invalid resource type + " + typeReference);

				resourceType.getJSONArray(ModelStructureConstants.NAMED_RESOURCES)
						.put(new JSONObject().put(ModelStructureConstants.NAME, name));
				continue;
			}

			if (entity instanceof ru.bmstu.rk9.rao.rao.Result) {
				simulatorPreinitializationInfo.modelStructure.getJSONArray(ModelStructureConstants.RESULTS)
						.put(new JSONObject().put(ModelStructureConstants.NAME, name));
				continue;
			}
		}

		List<Class<?>> varconstClasses = new ArrayList<>();

		// going through classes that are declared at initialization scope nested class
		// in model
		for (Class<?> nestedModelClass : initializationScope.getDeclaredClasses()) {
			// TODO make sure that the info that this class is not static is marked
			if (VarConst.class.isAssignableFrom(nestedModelClass)) {
				varconstClasses.add(nestedModelClass);
				continue;
			}
			
			if (Logic.class.isAssignableFrom(nestedModelClass)) {
				decisionPointClasses.add(nestedModelClass);
				continue;
			}
			if (Search.class.isAssignableFrom(nestedModelClass)) {
				decisionPointClasses.add(nestedModelClass);
				continue;
			}

			if (AnimationFrame.class.isAssignableFrom(nestedModelClass)) {
				animationClasses.add(nestedModelClass);
				continue;
			}

			if (Pattern.class.isAssignableFrom(nestedModelClass)) {
				tupleClasses.add(nestedModelClass);
				// FIXME this workaround makes sure that nested class in
				// patterns initialize and get into classloader, proper solution
				// is needed
				nestedModelClass.getDeclaredClasses();
				continue;
			}

		}

		/** look only for abstract results */
		for (Field field : initializationScope.getDeclaredFields()) {
			if (AbstractResult.class.isAssignableFrom(field.getType()))
				resultFields.add(field);
		}
		
		simulatorInitializationInfo.setResultFields(resultFields);
		simulatorInitializationInfo.setDecisionPointClasses(decisionPointClasses);
		// setUpBlocks();

		for (Class<?> varconstClass : varconstClasses) {
			Constructor<?> constructor = ReflectionUtils.safeGetConstructor(varconstClass);
			constructor.setAccessible(true);
			VarConst varconst = (VarConst) constructor.newInstance();
			varconsts.add(varconst);
		}
		
		for (Class<?> animationClass : animationClasses) {
			Constructor<?> constructor = 
					ReflectionUtils.safeGetConstructor(animationClass, simulatorCommonModelInfo.getInitializationScopeClass());

			animationFrames.add(constructor);
		}
		/** 27/03/2021 ??? not sure where it is used */
		// for (Method method : modelClass.getDeclaredMethods()) {
		// if (!method.getReturnType().equals(Boolean.TYPE))
		// continue;
		//
		// if (method.getParameterCount() > 0)
		// continue;
		//
		// Supplier<Boolean> supplier = () -> {
		// try {
		// return (boolean) method.invoke(null);
		// } catch (IllegalAccessException | IllegalArgumentException |
		// InvocationTargetException e) {
		// e.printStackTrace();
		// throw new RaoLibException("Internal error invoking function " +
		// method.getName());
		// }
		// };
		// modelContentsInfo.booleanFunctions.put(NamingHelper.createFullNameForMember(method),
		// supplier);
		// }
	}

	private void setUpBlocks() throws ClassNotFoundException, IOException, CoreException {
		// TODO this is connected to blocks functionality (probably), let's check for it later
		for (IResource processFile : BuildUtil.getAllFilesInProject(project, "proc")) {
			ProcessModelNode model = ProcessEditor.readModelFromFile((IFile) processFile);
			if (model == null)
				model = new ProcessModelNode();
			List<Block> blocks = BlockConverter.convertModelToBlocks(model, modelContentsInfo);
			simulatorInitializationInfo.getProcessBlocks().addAll(blocks);
		}
	}

	public final List<Constructor<?>> getAnimationFrames() {
		return animationFrames;
	}
	
	public List<VarConst> getVarConsts() {
		return varconsts;
	}

	public final void closeClassLoader() {
		if (classLoader != null) {
			try {
				classLoader.close();
			} catch (IOException e) {
			}
		}
	}
}
