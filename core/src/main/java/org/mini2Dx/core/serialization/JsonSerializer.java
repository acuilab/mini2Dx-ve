/**
 * Copyright (c) 2015 See AUTHORS file
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the mini2Dx nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mini2Dx.core.serialization;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mini2Dx.core.serialization.annotation.ConstructorArg;
import org.mini2Dx.core.serialization.annotation.NonConcrete;
import org.mini2Dx.core.serialization.annotation.PostDeserialize;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.Annotation;
import com.badlogic.gdx.utils.reflect.ArrayReflection;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/**
 * Serializes objects to/from JSON based on
 * {@link org.mini2Dx.core.serialization.annotation.Field} annotations
 */
@SuppressWarnings("unchecked")
public class JsonSerializer {
	private final Map<String, Method[]> methodCache = new HashMap<String, Method[]>();
	private final Map<String, Field[]> fieldCache = new HashMap<String, Field[]>();

	/**
	 * Reads a JSON document and converts it into an object of the specified
	 * type
	 * 
	 * @param fileHandle
	 *            The {@link FileHandle} for the JSON document
	 * @param clazz
	 *            The {@link Class} to convert the document to
	 * @return The object deserialized from JSON
	 * @throws SerializationException
	 *             Thrown when the data is invalid
	 */
	public <T> T fromJson(FileHandle fileHandle, Class<T> clazz) throws SerializationException {
		return deserialize(new JsonReader().parse(fileHandle), clazz);
	}

	/**
	 * Reads a JSON document and converts it into an object of the specified
	 * type
	 * 
	 * @param json
	 *            The JSON document
	 * @param clazz
	 *            The {@link Class} to convert the document to
	 * @return The object deserialized from JSON
	 * @throws SerializationException
	 *             Thrown when the data is invalid
	 */
	public <T> T fromJson(String json, Class<T> clazz) throws SerializationException {
		return deserialize(new JsonReader().parse(json), clazz);
	}

	/**
	 * Writes a JSON document by searching the object for
	 * {@link org.mini2Dx.core.serialization.annotation.Field} annotations
	 * 
	 * @param fileHandle
	 *            The {@link FileHandle} to write to
	 * @param object
	 *            The object to convert to JSON
	 * @throws SerializationException
	 *             Thrown when the object is invalid
	 */
	public <T> void toJson(FileHandle fileHandle, T object) throws SerializationException {
		toJson(fileHandle, object, false);
	}

	/**
	 * Writes a JSON document by searching the object for
	 * {@link org.mini2Dx.core.serialization.annotation.Field} annotations
	 * 
	 * @param fileHandle
	 *            The {@link FileHandle} to write to
	 * @param object
	 *            The object to convert to JSON
	 * @param prettyPrint
	 *            Set to true if the JSON should be prettified
	 * @throws SerializationException
	 *             Thrown when the object is invalid
	 */
	public <T> void toJson(FileHandle fileHandle, T object, boolean prettyPrint) throws SerializationException {
		String json = toJson(object, prettyPrint);
		fileHandle.writeString(json, false);
	}

	/**
	 * Writes a JSON document by searching the object for
	 * {@link org.mini2Dx.core.serialization.annotation.Field} annotations
	 * 
	 * @param object
	 *            The object to convert to JSON
	 * @return The object serialized as JSON
	 * @throws SerializationException
	 *             Thrown when the object is invalid
	 */
	public <T> String toJson(T object) throws SerializationException {
		return toJson(object, false);
	}

	/**
	 * Writes a JSON document by searching the object for
	 * {@link org.mini2Dx.core.serialization.annotation.Field} annotations
	 * 
	 * @param object
	 *            The object to convert to JSON
	 * @param prettyPrint
	 *            Set to true if the JSON should be prettified
	 * @return The object serialized as JSON
	 * @throws SerializationException
	 *             Thrown when the object is invalid
	 */
	public <T> String toJson(T object, boolean prettyPrint) throws SerializationException {
		StringWriter writer = new StringWriter();
		Json json = new Json();
		json.setOutputType(OutputType.json);
		json.setWriter(writer);

		writeObject(null, object, null, json);

		String result = writer.toString();
		try {
			writer.close();
		} catch (IOException e) {
			throw new SerializationException(e);
		}
		if (prettyPrint) {
			return json.prettyPrint(result);
		}
		return result;
	}
	
	private <T> void callPostDeserializeMethods(T object, Class<?> clazz) throws SerializationException {
		Class<?> currentClass = clazz;
		while (currentClass != null && !currentClass.equals(Object.class)) {
			final String className = currentClass.getName();
			if(!methodCache.containsKey(className)) {
				methodCache.put(className, ClassReflection.getDeclaredMethods(currentClass));
			}
			final Method [] methods = methodCache.get(className);

			for(Method method : methods) {
				if(method.isAnnotationPresent(PostDeserialize.class)) {
					try {
						method.invoke(object);
					} catch (ReflectionException e) {
						throw new SerializationException(e);
					}
				}
			}
			currentClass = currentClass.getSuperclass();
		}
	}

	private <T> void writePrimitive(String fieldName, Object value, Json json) {
		if (fieldName != null) {
			json.writeValue(fieldName, value);
		} else {
			json.writeValue(value);
		}
	}

	private <T> void writeArray(Field field, Object array, Json json) throws SerializationException {
		if (field != null) {
			json.writeArrayStart(field.getName());
		} else {
			json.writeArrayStart();
		}
		
		int arrayLength = Array.getLength(array);
		for (int i = 0; i < arrayLength; i++) {
			writeObject(field, Array.get(array, i), null, json);
		}
		json.writeArrayEnd();
	}
	
	private <T> void writeGdxArray(Field field, com.badlogic.gdx.utils.Array array, Json json) throws SerializationException {
		if (field != null) {
			json.writeArrayStart(field.getName());
		} else {
			json.writeArrayStart();
		}
		
		int arrayLength = array.size;
		for (int i = 0; i < arrayLength; i++) {
			writeObject(field, array.get(i), null, json);
		}
		json.writeArrayEnd();
	}
	
	private <T> void writeObjectMap(Field field, ObjectMap map, Json json) throws SerializationException {
		if (field != null) {
			json.writeObjectStart(field.getName());
		} else {
			json.writeObjectStart();
		}
		
		ObjectMap.Entries entries = map.iterator();
		while(entries.hasNext()) {
			ObjectMap.Entry entry = entries.next();
			writeObject(field, entry.value, entry.key.toString(), json);
		}
		json.writeObjectEnd();
	}

	private <T> void writeMap(Field field, Map map, Json json) throws SerializationException {
		if (field != null) {
			json.writeObjectStart(field.getName());
		} else {
			json.writeObjectStart();
		}
		
		for (Object key : map.keySet()) {
			writeObject(field, map.get(key), key.toString(), json);
		}
		json.writeObjectEnd();
	}
	
	private <T> void writeClassFieldIfRequired(Field fieldDefinition, T object, String fieldName, Json json) throws SerializationException {
		if (fieldDefinition == null) {
			return;
		}
		Class<?> clazz = object.getClass();
		Class<?> fieldDefinitionClass = fieldDefinition.getType();
		
		if(fieldDefinitionClass.isArray()) {
			Class<?> arrayComponentType = fieldDefinitionClass.getComponentType();
			if(arrayComponentType.isInterface() && arrayComponentType.getAnnotation(NonConcrete.class) == null) {
				throw new SerializationException("Cannot serialize interface unless it has a @" + NonConcrete.class.getSimpleName() + " annotation");
			}
			writePrimitive("class", clazz.getName(), json);
			return;
		}
		if(Collection.class.isAssignableFrom(fieldDefinitionClass)) {
			Class<?> valueClass = fieldDefinition.getElementType(0);
			if(valueClass.isInterface() && valueClass.getAnnotation(NonConcrete.class) == null) {
				throw new SerializationException("Cannot serialize interface unless it has a @" + NonConcrete.class.getSimpleName() + " annotation");
			}
			writePrimitive("class", clazz.getName(), json);
			return;
		}
		if(Map.class.isAssignableFrom(fieldDefinitionClass)) {
			Class<?> valueClass = fieldDefinition.getElementType(1);
			if(valueClass.isInterface() && valueClass.getAnnotation(NonConcrete.class) == null) {
				throw new SerializationException("Cannot serialize interface unless it has a @" + NonConcrete.class.getSimpleName() + " annotation");
			}
			writePrimitive("class", clazz.getName(), json);
			return;
		}
		if(fieldDefinitionClass.isInterface()) {
			if(fieldDefinitionClass.getAnnotation(NonConcrete.class) == null) {
				throw new SerializationException("Cannot serialize interface unless it has a @" + NonConcrete.class.getSimpleName() + " annotation");
			}
			writePrimitive("class", clazz.getName(), json);
			return;
		}
		if(Modifier.isAbstract(fieldDefinitionClass.getModifiers())) {
			if(fieldDefinitionClass.getAnnotation(NonConcrete.class) == null) {
				throw new SerializationException("Cannot serialize abstract class unless it has a @" + NonConcrete.class.getSimpleName() + " annotation");
			}
			writePrimitive("class", clazz.getName(), json);
			return;
		} 
	}

	private <T> void writeObject(Field fieldDefinition, T object, String fieldName, Json json) throws SerializationException {
		try {
			if (object == null) {
				writePrimitive(fieldName, null, json);
				return;
			}

			Class<?> clazz = object.getClass();

			if (isPrimitive(clazz) || clazz.equals(String.class)) {
				writePrimitive(fieldName, object, json);
				return;
			}
			if (clazz.isEnum() || clazz.getSuperclass().isEnum()) {
				writePrimitive(fieldName, object.toString(), json);
				return;
			}
			if (clazz.isArray()) {
				writeArray(fieldDefinition, object, json);
				return;
			}
			if (Collection.class.isAssignableFrom(clazz)) {
				Collection collection = (Collection) object;
				writeArray(fieldDefinition, collection.toArray(), json);
				return;
			}
			if (Map.class.isAssignableFrom(clazz)) {
				writeMap(fieldDefinition, (Map) object, json);
				return;
			}
			if (ObjectMap.class.isAssignableFrom(clazz)) {
				writeObjectMap(fieldDefinition, (ObjectMap) object, json);
				return;
			}
			if (com.badlogic.gdx.utils.Array.class.isAssignableFrom(clazz)) {
				writeGdxArray(fieldDefinition, (com.badlogic.gdx.utils.Array) object, json);
				return;
			}

			if (fieldName == null) {
				json.writeObjectStart();
			} else {
				json.writeObjectStart(fieldName);
			}
			writeClassFieldIfRequired(fieldDefinition, object, fieldName, json);

			Class<?> currentClass = clazz;
			while (currentClass != null && !currentClass.equals(Object.class)) {
				final String className = currentClass.getName();
				if(!fieldCache.containsKey(className)) {
					fieldCache.put(className, ClassReflection.getDeclaredFields(currentClass));
				}
				if(!methodCache.containsKey(className)) {
					methodCache.put(className, ClassReflection.getDeclaredMethods(currentClass));
				}

				final Method [] methods = methodCache.get(className);
				final Field [] fields = fieldCache.get(className);

				for (Field field : fields) {
					field.setAccessible(true);
					Annotation annotation = field
							.getDeclaredAnnotation(org.mini2Dx.core.serialization.annotation.Field.class);

					if (annotation == null) {
						continue;
					}
					org.mini2Dx.core.serialization.annotation.Field fieldAnnotation = annotation
							.getAnnotation(org.mini2Dx.core.serialization.annotation.Field.class);

					if (!fieldAnnotation.optional() && field.get(object) == null) {
						throw new RequiredFieldException(currentClass, field.getName());
					}
					writeObject(field, field.get(object), field.getName(), json);
				}
				for(Method method : methods) {
					if(method.getParameterTypes().length > 0) {
						continue;
					}
					Annotation annotation = method.getDeclaredAnnotation(ConstructorArg.class);
					if(annotation == null) {
						continue;
					}
					ConstructorArg constructorArg = annotation.getAnnotation(ConstructorArg.class);
					writeObject(null, method.invoke(object), constructorArg.name(), json); ;
				}
				currentClass = currentClass.getSuperclass();
			}
			
			//Check for @ConstructorArg annotations in interface methods
			Class<?> [] interfaces = clazz.getInterfaces();
			for(int i = 0; i < interfaces.length; i++) {
				final String className = interfaces[i].getName();
				if(!methodCache.containsKey(className)) {
					methodCache.put(className, ClassReflection.getDeclaredMethods(interfaces[i]));
				}
				final Method [] methods = methodCache.get(className);

				for(Method method : methods) {
					if(method.getParameterTypes().length > 0) {
						continue;
					}
					Annotation annotation = method.getDeclaredAnnotation(ConstructorArg.class);
					if(annotation == null) {
						continue;
					}
					ConstructorArg constructorArg = annotation.getAnnotation(ConstructorArg.class);
					writeObject(null, method.invoke(object), constructorArg.name(), json); ;
				}
			}

			json.writeObjectEnd();
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}

	private <T> T construct(JsonValue objectRoot, Class<?> clazz) throws InstantiationException, IllegalAccessException,
			SerializationException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
		Constructor<?>[] constructors = clazz.getConstructors();
		// Single constructor with no args
		if (constructors.length == 1 && constructors[0].getParameterAnnotations().length == 0) {
			return (T) clazz.newInstance();
		}

		Constructor bestMatchedConstructor = null;
		List<ConstructorArg> detectedAnnotations = new ArrayList<ConstructorArg>(1);

		for (int i = 0; i < constructors.length; i++) {
			detectedAnnotations.clear();
			boolean allAnnotated = true;

			for (int j = 0; j < constructors[i].getParameterAnnotations().length; j++) {
				java.lang.annotation.Annotation[] annotations = constructors[i].getParameterAnnotations()[j];
				if (annotations.length == 0) {
					allAnnotated = false;
					break;
				}

				boolean hasConstructorArgAnnotation = false;
				for (int k = 0; k < annotations.length; k++) {
					if (!annotations[i].annotationType().isAssignableFrom(ConstructorArg.class)) {
						continue;
					}
					ConstructorArg constructorArg = (ConstructorArg) annotations[i];
					if (objectRoot.get(constructorArg.name()) == null) {
						continue;
					}
					detectedAnnotations.add(constructorArg);
					hasConstructorArgAnnotation = true;
					break;
				}
				if (!hasConstructorArgAnnotation) {
					allAnnotated = false;
				}
			}
			if (!allAnnotated) {
				continue;
			}
			if (bestMatchedConstructor == null) {
				bestMatchedConstructor = constructors[i];
			} else if (detectedAnnotations.size() > bestMatchedConstructor.getParameterAnnotations().length) {
				bestMatchedConstructor = constructors[i];
			}
		}
		if (bestMatchedConstructor == null) {
			throw new SerializationException("Could not find suitable constructor for class " + clazz.getName());
		}
		if (detectedAnnotations.size() == 0) {
			return (T) clazz.newInstance();
		}

		Object[] constructorParameters = new Object[detectedAnnotations.size()];
		for (int i = 0; i < detectedAnnotations.size(); i++) {
			ConstructorArg constructorArg = detectedAnnotations.get(i);
			constructorParameters[i] = deserialize(objectRoot.get(constructorArg.name()), constructorArg.clazz());
			objectRoot.remove(constructorArg.name());
		}
		return (T) bestMatchedConstructor.newInstance(constructorParameters);
	}
	
	private Class<?> determineImplementation(JsonValue objectRoot, Class<?> clazz) throws SerializationException, ClassNotFoundException {
		if(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
			JsonValue classField = objectRoot.get("class");
			if(classField == null) {
				throw new SerializationException("No class field found for deserializing " + clazz.getName());
			}
			clazz = Class.forName(classField.asString());
		}
		return clazz;
	}

	private <T> T deserialize(JsonValue objectRoot, Class<T> fieldClass) throws SerializationException {
		try {
			if (objectRoot.isNull()) {
				return null;
			}
			if (objectRoot.isObject()) {
				Class<?> clazz = determineImplementation(objectRoot, fieldClass);
				T result = construct(objectRoot, clazz);
				Class<?> currentClass = clazz;
				while (currentClass != null && !currentClass.equals(Object.class)) {
					for (Field field : ClassReflection.getDeclaredFields(currentClass)) {
						field.setAccessible(true);
						Annotation annotation = field
								.getDeclaredAnnotation(org.mini2Dx.core.serialization.annotation.Field.class);

						if (annotation == null) {
							continue;
						}
						org.mini2Dx.core.serialization.annotation.Field fieldAnnotation = annotation
								.getAnnotation(org.mini2Dx.core.serialization.annotation.Field.class);

						JsonValue value = objectRoot.get(field.getName());
						if (value == null || value.isNull()) {
							if (!fieldAnnotation.optional()) {
								throw new RequiredFieldException(currentClass, field.getName());
							}
							continue;
						}
						setField(result, field, value);
					}
					currentClass = currentClass.getSuperclass();
				}
				callPostDeserializeMethods(result, clazz);
				return result;
			}
			if (objectRoot.isArray()) {
				Class<?> arrayType = fieldClass.getComponentType();
				Object array = ArrayReflection.newInstance(arrayType, objectRoot.size);
				for (int i = 0; i < objectRoot.size; i++) {
					Array.set(array, i, deserialize(objectRoot.get(i), arrayType));
				}
				return (T) array;
			}
			if (fieldClass.isEnum()) {
				return (T) Enum.valueOf((Class<Enum>) fieldClass, objectRoot.asString());
			}

			if (fieldClass.equals(Boolean.TYPE) || fieldClass.equals(Boolean.class)) {
				return (T) ((Boolean) objectRoot.asBoolean());
			} else if (fieldClass.equals(Byte.TYPE) || fieldClass.equals(Byte.class)) {
				return (T) ((Byte) objectRoot.asByte());
			} else if (fieldClass.equals(Character.TYPE) || fieldClass.equals(Character.class)) {
				return (T) ((Character) objectRoot.asChar());
			} else if (fieldClass.equals(Double.TYPE) || fieldClass.equals(Double.class)) {
				return (T) ((Double) objectRoot.asDouble());
			} else if (fieldClass.equals(Float.TYPE) || fieldClass.equals(Float.class)) {
				return (T) ((Float) objectRoot.asFloat());
			} else if (fieldClass.equals(Integer.TYPE) || fieldClass.equals(Integer.class)) {
				return (T) ((Integer) objectRoot.asInt());
			} else if (fieldClass.equals(Long.TYPE) || fieldClass.equals(Long.class)) {
				return (T) ((Long) objectRoot.asLong());
			} else if (fieldClass.equals(Short.TYPE) || fieldClass.equals(Short.class)) {
				return (T) ((Short) objectRoot.asShort());
			} else {
				return (T) objectRoot.asString();
			}
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SerializationException(e);
		}
	}

	private <T> void setField(T targetObject, Field field, JsonValue value) throws SerializationException {
		try {
			Class<?> clazz = field.getType();
			if (clazz.isArray()) {
				setArrayField(targetObject, field, clazz, value);
				return;
			}
			if (clazz.isEnum()) {
				if(field.isFinal()) {
					throw new SerializationException("Cannot use @Field on final enum fields. Use the @ConstructorArg method instead.");
				}
				field.set(targetObject, Enum.valueOf((Class<? extends Enum>) clazz, value.asString()));
				return;
			}
			if (!clazz.isPrimitive()) {
				if (clazz.equals(String.class)) {
					if(field.isFinal()) {
						throw new SerializationException("Cannot use @Field on final String fields. Use the @ConstructorArg method instead.");
					}
					field.set(targetObject, value.asString());
				} else if (Map.class.isAssignableFrom(clazz)) {
					setMapField(targetObject, field, clazz, value);
				} else if (Collection.class.isAssignableFrom(clazz)) {
					setCollectionField(targetObject, field, clazz, value);
				} else if (ObjectMap.class.isAssignableFrom(clazz)) {
					setObjectMapField(targetObject, field, clazz, value);
				} else if (com.badlogic.gdx.utils.Array.class.isAssignableFrom(clazz)) {
					setGdxArrayField(targetObject, field, clazz, value);
				} else {
					if(field.isFinal()) {
						throw new SerializationException("Cannot use @Field on final " + clazz.getName() +" fields.");
					}
					field.set(targetObject, deserialize(value, clazz));
				}
				return;
			}
			if(field.isFinal()) {
				throw new SerializationException("Cannot use @Field on final " + clazz.getName() +" fields. Use the @ConstructorArg method instead.");
			}
			field.set(targetObject, deserialize(value, clazz));
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SerializationException(e);
		}
	}
	
	private <T> void setGdxArrayField(T targetObject, Field field, Class<?> clazz, JsonValue value)
		throws SerializationException {
		try {
			Class<?> valueClass = field.getElementType(0);
			Class<?> implementationClass = determineImplementation(value, clazz);
			
			com.badlogic.gdx.utils.Array targetArray = null;
			if(field.isFinal()) {
				targetArray = (com.badlogic.gdx.utils.Array) field.get(targetObject);
			} else {
				targetArray = construct(value, implementationClass);
			}
			
			for(int i = 0; i < value.size; i++) {
				targetArray.add(deserialize(value.get(i), valueClass));
			}
			
			if(!field.isFinal()) {
				field.set(targetObject, targetArray);
			}
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			e.getMessage();
			throw new SerializationException(e);
		}
	}
	
	private <T> void setArrayField(T targetObject, Field field, Class<?> clazz, JsonValue value)
			throws SerializationException {
		try {
			if (clazz.equals(boolean[].class)) {
				boolean [] result = value.asBooleanArray();
				if(field.isFinal()) {
					Object targetArray = field.get(targetObject);
					for(int i = 0; i < result.length; i++) {
						ArrayReflection.set(targetArray, i, result[i]);
					}
				} else {
					field.set(targetObject, result);
				}
			} else if (clazz.equals(byte[].class)) {
				byte [] result = value.asByteArray();
				if(field.isFinal()) {
					Object targetArray = field.get(targetObject);
					for(int i = 0; i < result.length; i++) {
						ArrayReflection.set(targetArray, i, result[i]);
					}
				} else {
					field.set(targetObject, result);
				}
			} else if (clazz.equals(char[].class)) {
				char [] result = value.asCharArray();
				if(field.isFinal()) {
					Object targetArray = field.get(targetObject);
					for(int i = 0; i < result.length; i++) {
						ArrayReflection.set(targetArray, i, result[i]);
					}
				} else {
					field.set(targetObject, result);
				}
			} else if (clazz.equals(double[].class)) {
				double [] result = value.asDoubleArray();
				if(field.isFinal()) {
					Object targetArray = field.get(targetObject);
					for(int i = 0; i < result.length; i++) {
						ArrayReflection.set(targetArray, i, result[i]);
					}
				} else {
					field.set(targetObject, result);
				}
			} else if (clazz.equals(float[].class)) {
				float [] result = value.asFloatArray();
				if(field.isFinal()) {
					Object targetArray = field.get(targetObject);
					for(int i = 0; i < result.length; i++) {
						ArrayReflection.set(targetArray, i, result[i]);
					}
				} else {
					field.set(targetObject, result);
				}
			} else if (clazz.equals(int[].class)) {
				int [] result = value.asIntArray();
				if(field.isFinal()) {
					Object targetArray = field.get(targetObject);
					for(int i = 0; i < result.length; i++) {
						ArrayReflection.set(targetArray, i, result[i]);
					}
				} else {
					field.set(targetObject, result);
				}
			} else if (clazz.equals(long[].class)) {
				long [] result = value.asLongArray();
				if(field.isFinal()) {
					Object targetArray = field.get(targetObject);
					for(int i = 0; i < result.length; i++) {
						ArrayReflection.set(targetArray, i, result[i]);
					}
				} else {
					field.set(targetObject, result);
				}
			} else if (clazz.equals(short[].class)) {
				short [] result = value.asShortArray();
				if(field.isFinal()) {
					Object targetArray = field.get(targetObject);
					for(int i = 0; i < result.length; i++) {
						ArrayReflection.set(targetArray, i, result[i]);
					}
				} else {
					field.set(targetObject, result);
				}
			} else if (clazz.equals(String[].class)) {
				String [] result = value.asStringArray();
				if(field.isFinal()) {
					Object targetArray = field.get(targetObject);
					for(int i = 0; i < result.length; i++) {
						ArrayReflection.set(targetArray, i, result[i]);
					}
				} else {
					field.set(targetObject, result);
				}
			} else {
				Object result = deserialize(value, clazz);
				if(field.isFinal()) {
					Object targetArray = field.get(targetObject);
					int length = ArrayReflection.getLength(result);
					for(int i = 0; i < length; i++) {
						ArrayReflection.set(targetArray, i, ArrayReflection.get(result, i));
					}
				} else {
					field.set(targetObject, result);
				}
			}
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}

	private <T> void setCollectionField(T targetObject, Field field, Class<?> clazz, JsonValue value)
			throws SerializationException {
		try {
			Class<?> valueClass = field.getElementType(0);

			Collection collection = null;
			
			if(field.isFinal()) {
				collection = (Collection) field.get(targetObject);
			} else {
				collection = (Collection) (clazz.isInterface() ? new ArrayList()
						: ClassReflection.newInstance(clazz));
			}
			for (int i = 0; i < value.size; i++) {
				collection.add(deserialize(value.get(i), valueClass));
			}
			
			if(!field.isFinal()) {
				field.set(targetObject, collection);
			}
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}

	private <T> void setMapField(T targetObject, Field field, Class<?> clazz, JsonValue value)
			throws SerializationException {
		try {
			Map map = null;
			
			if(field.isFinal()) {
				map = (Map) field.get(targetObject);
			} else {
				map = (Map) (clazz.isInterface() ? new HashMap() : ClassReflection.newInstance(clazz));
			}
			
			Class<?> keyClass = field.getElementType(0);
			Class<?> valueClass = field.getElementType(1);

			for (int i = 0; i < value.size; i++) {
				map.put(parseMapKey(value.get(i).name, keyClass), deserialize(value.get(i), valueClass));
			}
			
			if(!field.isFinal()) {
				field.set(targetObject, map);
			}
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}
	
	private <T> void setObjectMapField(T targetObject, Field field, Class<?> clazz, JsonValue value)
			throws SerializationException {
		try {
			ObjectMap map = null;
			
			if(field.isFinal()) {
				map = (ObjectMap) field.get(targetObject);
			} else {
				map = new ObjectMap();
			}
			
			Class<?> keyClass = field.getElementType(0);
			Class<?> valueClass = field.getElementType(1);

			for (int i = 0; i < value.size; i++) {
				map.put(parseMapKey(value.get(i).name, keyClass), deserialize(value.get(i), valueClass));
			}
			
			if(!field.isFinal()) {
				field.set(targetObject, map);
			}
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}

	private boolean isPrimitive(Class<?> clazz) {
		if (clazz.isPrimitive()) {
			return true;
		}
		if (clazz.equals(Boolean.class)) {
			return true;
		}
		if (clazz.equals(Byte.class)) {
			return true;
		}
		if (clazz.equals(Character.class)) {
			return true;
		}
		if (clazz.equals(Double.class)) {
			return true;
		}
		if (clazz.equals(Float.class)) {
			return true;
		}
		if (clazz.equals(Integer.class)) {
			return true;
		}
		if (clazz.equals(Long.class)) {
			return true;
		}
		if (clazz.equals(Short.class)) {
			return true;
		}
		return false;
	}

	private <T> T parseMapKey(String value, Class<T> clazz) {
		if (clazz.equals(Boolean.TYPE) || clazz.equals(Boolean.class)) {
			return (T) new Boolean(value);
		}
		if (clazz.equals(Byte.TYPE) || clazz.equals(Byte.class)) {
			return (T) new Byte(value);
		}
		if (clazz.equals(Character.TYPE) || clazz.equals(Character.class)) {
			return (T) new Character(value.charAt(0));
		}
		if (clazz.equals(Double.TYPE) || clazz.equals(Double.class)) {
			return (T) new Double(value);
		}
		if (clazz.equals(Float.TYPE) || clazz.equals(Float.class)) {
			return (T) new Float(value);
		}
		if (clazz.equals(Integer.TYPE) || clazz.equals(Integer.class)) {
			return (T) new Integer(value);
		}
		if (clazz.equals(Long.TYPE) || clazz.equals(Long.class)) {
			return (T) new Long(value);
		}
		if (clazz.equals(Short.TYPE) || clazz.equals(Short.class)) {
			return (T) new Short(value);
		}
		if (clazz.isEnum()) {
			return (T) Enum.valueOf((Class<Enum>) clazz, value);
		}
		return (T) value;
	}
}
