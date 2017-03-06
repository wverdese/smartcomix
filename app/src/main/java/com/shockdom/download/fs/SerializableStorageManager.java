package com.shockdom.download.fs;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Context;

/**
 * Classe per la lettura/scrittura di Oggetti dal FileSystem, identificati da una stringa. E' possibile serializzare un Oggetto solo se estende l'interfaccia Serializable.
 * Tramite questa classe � possibile serializzare una risorsa in Cache (svuotabile dalle impostazioni), o in spazio di Storage (svuotabile disinstallando l'applicazione), o in una qualunque locazione di memoria, specificata mediante un path.
 * E' possibile inoltre decidere di archiviare la risorsa in Memoria Esterna (sdcard), Interna (/data/data), o delegare a questa classe la decisione.
 * 
 * @author Walter Verdese
 *
 */
public class SerializableStorageManager extends
		FileSystemStorageManager<String, Serializable, Serializable> {
	
	/**
	 * Costruttore a visibilit� ridotta: � preferibile istanziare questo StorageManager come Singleton, in modo tale che questa classe costituisca un singolo entry point per la gestione del File System dell'intera applicazione.
	 */
	public SerializableStorageManager(Context c) {
		super(c);
	}
	

	/**
	 * Metodo per la scrittura di un Oggetto su File.
	 * Tutte le scritture implementate da questa classe invocano questo metodo per operare sul FileSystem.
	 * Eventuali invocazioni concorrenti di questo metodo vengono sincronizzate.
	 * 
	 * @param filePath il path assoluto del File che conterr� l'Oggetto serializzato.
	 * @param obj l'oggetto (serializzabile).
	 * @return true se l'operazione di scrittura � andata a buon fine, false altrimenti.
	 * @throws Exception se � avvenuta un'eccezione durante la scrittura.
	 */
	@Override
	public synchronized boolean write(String filePath, Serializable obj) throws Exception {
		BufferedOutputStream outStream = null;
		if (filePath != null) {
			File outputFile = new File(filePath);
			
			//check parent
			File parent = outputFile.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			
			outStream = new BufferedOutputStream(new FileOutputStream(outputFile));
		}
		File result = null;
		if (outStream != null) {
			Exception e = null;
			boolean isOk = false;
			try {
				//se faccio cos� poi non posso interrompere l'operazione di scrittura
				/*ObjectOutputStream os = new ObjectOutputStream(outStream);
				os.writeObject(obj);
				os.close();*/
				
				//Object -> ByteArray -> InputStream = lettura/scrittura in memoria principale
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
			    ObjectOutputStream oos = new ObjectOutputStream(baos);
			    oos.writeObject(obj);
			    oos.flush();
			    oos.close();
			    InputStream inStream = new ByteArrayInputStream(baos.toByteArray());
				
			    //ora posso chiamare la write su disco di livello superiore (che � interrompibile)
			    isOk = write(inStream, outStream);
			    
				//Log.i("FILE DEBUG", "Writing loop successfully completed.");
			} catch (Exception e1) {
				e = e1;
				//Log.i("FILE DEBUG", "Writing loop has crashed.");
			}
			//Log.i("FILE DEBUG", "Start fetching writing result.");
			result = new File(filePath);
			if (!isOk && result.exists()) {
				//Log.i("FILE DEBUG", "Result found. Deleting partial file.");
				result.delete();
				//Log.i("FILE DEBUG", "Partial file deleted.");
				result = null;
			}
			else {
				//Log.i("FILE DEBUG", "Result found and returned.");
			}
			if (e != null) {
				throw e;
			}
		}
		return result != null;
	}

	/**
	 * Metodo per la lettura di un Oggetto da File.
	 * Tutte le letture implementate da questa classe invocano questo metodo per operare sul FileSystem.
	 * Eventuali invocazioni concorrenti di questo metodo vengono sincronizzate.
	 * 
	 * @param filePath il path assoluto del File contenente l'Oggetto serializzato.
	 * @return l'Oggetto risultante dalla lettura se l'operazione � andata a buon fine, null altrimenti.
	 */
	@Override
	public synchronized Serializable read(String filePath) {
		File file = new File(filePath);
		if (file != null && file.exists()) {
			//Log.i(TAG, "leggo dalla memoria ESTERNA");
			try{
				FileInputStream fis = new FileInputStream(file);
				ObjectInputStream is = new ObjectInputStream(fis);
				Object obj = is.readObject();
				is.close();
				fis.close();
				if (obj instanceof Serializable) {
					return (Serializable) obj;
				}
			}catch (Exception e) {
				e.printStackTrace();
			}			
		}
		return null;
	}

    /**
     * Metodo di comodo per la scrittura di un Oggetto in un File in Memoria di Cache (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
     * 
     * @param obj l'oggetto (serializzabile).
     * @return il nome del file serializzato (senza path).
     * @throws Exception se � avvenuta un'eccezione durante la scrittura.
     */
	@Override
	public String writeInCache(Serializable obj) throws Exception {
		boolean isOk = false;
		Exception e = null;
		//Log.i("FILE DEBUG", "Starting writing operation from stream.");
		String fileName = getFileNameFromURL(""+System.currentTimeMillis());
		try {
			//l'implementazione esegue una scrittura in cache AUT l'altra
			if (isExternalStorageSupported()) {
				isOk = writeInExternalCache(null, fileName, obj);
			}
			else {
				isOk = writeInInternalCache(null, fileName, obj);
			}
		} catch (Exception e1) {
			e = e1;
		}
		//Log.i("FILE DEBUG", "Writing operation completed.");
		if (e != null) {
			throw e;
		}
		if (!isOk) {
			fileName = null;
		}
		return fileName;
	}
	
    /**
     * Metodo di comodo per la scrittura di un Oggetto in un File in Memoria di Cache (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
     * 
     * @param obj l'oggetto (serializzabile).
     * @param fileName il nome del file da serializzare.
     * @return true se l'operazione � andata a buon fine, false altrimenti.
     * @throws Exception se � avvenuta un'eccezione durante la scrittura.
     */
	@Override
	public boolean writeInCache(String fileName, Serializable obj) throws Exception {
		boolean isOk = false;
		Exception e = null;
		//Log.i("FILE DEBUG", "Starting writing operation from stream.");
		try {
			//l'implementazione esegue una scrittura in cache AUT l'altra
			if (isExternalStorageSupported()) {
				isOk = writeInExternalCache(null, fileName, obj);
			}
			else {
				isOk = writeInInternalCache(null, fileName, obj);
			}
		} catch (Exception e1) {
			e = e1;
		}
		//Log.i("FILE DEBUG", "Writing operation completed.");
		if (e != null) {
			throw e;
		}
		return isOk;
	}

	/**
     * Metodo di comodo per la lettura di un Oggetto da File in Memoria di Cache (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
     * 
     * @param fileName il nome del file (senza path) contenuto in memoria di Cache da cercare.
     * @return l'Oggetto se la lettura � andata a buon fine, null altrimenti.
     */
	@Override
	public Serializable readFromCache(String fileName) {
		Serializable obj = readFromExternalCache(null, fileName);
		if (obj == null) {
			obj = readFromInternalCache(null, fileName);
		}
		return obj;
	}
	
	/**
     * Metodo generico per la lettura di un oggetto dalla Memoria di Cache (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
     * 
     * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Cache.
     * @param fileName il nome del file (senza path) contenuto in memoria di Cache da cercare.
     * @return l'Oggetto se la lettura � andata a buon fine, null altrimenti.
     */
	@Override
	public Serializable readFromCache(String subfolder, String fileName) {
		Serializable obj = readFromExternalCache(subfolder, fileName);
		if (obj == null) {
			obj = readFromInternalCache(subfolder, fileName);
		}
		return obj;
	}

	/**
     * Metodo di comodo per la scrittura di un Oggetto su File in Memoria di Cache Esterna.
     * 
     * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Cache.
     * @param fileName il nome del file da serializzare.
     * @param obj l'oggetto (serializzabile).
     * @return true se l'operazione � andata a buon fine, false altrimenti.
     * @throws Exception se � avvenuta un'eccezione durante la scrittura.
     */
	@Override
	public boolean writeInExternalCache(String subFolder, String fileName,
			Serializable obj) throws Exception {
		String path = getCacheFilePath(subFolder, fileName, true);
		return write(path, obj);
	}

	/**
	 * Metodo di comodo per la lettura di un Oggetto da File in Memoria di Cache Esterna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Cache.
	 * @param fileName il nome del file da cercare.
	 * @return il File, se la lettura � andata a buon fine, null altrimenti.
	 */
	@Override
	public Serializable readFromExternalCache(String subFolder, String fileName) {
		String path = getCacheFilePath(subFolder, fileName, true);
		return read(path);
	}

	/**
     * Metodo di comodo per la scrittura di un Oggetto su File in Memoria di Cache Interna.
     * 
     * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Cache.
     * @param fileName il nome del file da serializzare.
     * @param obj l'oggetto (serializzabile).
     * @return true se l'operazione � andata a buon fine, false altrimenti.
     * @throws Exception se � avvenuta un'eccezione durante la scrittura.
     */
	@Override
	public boolean writeInInternalCache(String subFolder, String fileName,
			Serializable obj) throws Exception {
		String path = getCacheFilePath(subFolder, fileName, false);
		return write(path, obj);
	}

	/**
	 * Metodo di comodo per la lettura di un Oggetto da File in Memoria di Cache Interna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Cache.
	 * @param fileName il nome del file da cercare.
	 * @return l'Oggetto, se la lettura � andata a buon fine, null altrimenti.
	 */
	@Override
	public Serializable readFromInternalCache(String subFolder, String fileName) {
		String path = getCacheFilePath(subFolder, fileName, false);
		return read(path);
	}

	/**
     * Metodo di comodo per la scrittura di un Oggetto in un File in Memoria di Storage (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
     * 
     * @param obj l'oggetto (serializzabile).
     * @return il nome del file serializzato (senza path).
     * @throws Exception se � avvenuta un'eccezione durante la scrittura.
     */
	@Override
	public String writeInStorage(Serializable obj) throws Exception {
		boolean isOk = false;
		Exception e = null;
		//Log.i("FILE DEBUG", "Starting writing operation from stream.");
		String fileName = getFileNameFromURL(""+System.currentTimeMillis());
		try {
			//l'implementazione esegue una scrittura in cache AUT l'altra
			if (isExternalStorageSupported()) {
				isOk = writeInExternalStorage(null, fileName, obj);
			}
			else {
				isOk = writeInInternalStorage(null, fileName, obj);
			}
		} catch (Exception e1) {
			e = e1;
		}
		//Log.i("FILE DEBUG", "Writing operation completed.");
		
		if (e != null) {
			throw e;
		}
		if (!isOk) {
			fileName = null;
		}
		return fileName;
	}
	
	/**
     * Metodo di comodo per la scrittura di un Oggetto in un File in Memoria di Storage (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
     * 
     * @param obj l'oggetto (serializzabile).
     * @param fileName il nome del file da serializzare.
     * @return true se l'operazione � andata a buon fine, false altrimenti.
     * @throws Exception se � avvenuta un'eccezione durante la scrittura.
     */
	@Override
	public boolean writeInStorage(String fileName, Serializable obj) throws Exception {
		return writeInStorage(null, fileName, obj);
	}

	public boolean writeInStorage(String subFolder, String fileName,  Serializable obj) throws Exception {
		boolean isOk = false;
		Exception e = null;
		//Log.i("FILE DEBUG", "Starting writing operation from stream.");
		try {
			//l'implementazione esegue una scrittura in cache AUT l'altra
			if (isExternalStorageSupported()) {
				isOk = writeInExternalStorage(subFolder, fileName, obj);
			}
			else {
				isOk = writeInInternalStorage(subFolder, fileName, obj);
			}
		} catch (Exception e1) {
			e = e1;
		}
		//Log.i("FILE DEBUG", "Writing operation completed.");

		if (e != null) {
			throw e;
		}
		return isOk;
	}

	/**
     * Metodo di comodo per la lettura di un Oggetto da File in Memoria di Storage (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
     * 
     * @param fileName il nome del file (senza path) contenuto in memoria di Storage da cercare.
     * @return l'Oggetto se la lettura � andata a buon fine, null altrimenti.
     */
	@Override
	public Serializable readFromStorage(String fileName) {
		return readFromStorage(null, fileName);
	}
	
	/**
     * Metodo di comodo per la lettura di un Oggetto da File in Memoria di Storage (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
     * 
     * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
     * @param fileName il nome del file (senza path) contenuto in memoria di Storage da cercare.
     * @return l'Oggetto se la lettura � andata a buon fine, null altrimenti.
     */
	@Override
	public Serializable readFromStorage(String subfolder, String fileName) {
		Serializable obj = readFromExternalStorage(subfolder, fileName);
		if (obj == null) {
			obj = readFromInternalStorage(subfolder, fileName);
		}
		return obj;
	}

	/**
     * Metodo di comodo per la scrittura di un Oggetto su File in Memoria di Storage Esterna.
     * 
     * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
     * @param fileName il nome del file da serializzare.
     * @param obj l'oggetto (serializzabile).
     * @return true se l'operazione � andata a buon fine, false altrimenti.
     * @throws Exception se � avvenuta un'eccezione durante la scrittura.
     */
	@Override
	public boolean writeInExternalStorage(String subFolder, String fileName,
			Serializable obj) throws Exception {
		String path = getStorageFilePath(subFolder, fileName, true);
		return write(path, obj);
	}

	/**
	 * Metodo di comodo per la lettura di un Oggetto da File in Memoria di Storage Esterna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
	 * @param fileName il nome del file da cercare.
	 * @return il File, se la lettura � andata a buon fine, null altrimenti.
	 */
	@Override
	public Serializable readFromExternalStorage(String subFolder, String fileName) {
		String path = getStorageFilePath(subFolder, fileName, true);
		return read(path);
	}

	/**
     * Metodo di comodo per la scrittura di un Oggetto su File in Memoria di Storage Interna.
     * 
     * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
     * @param fileName il nome del file da serializzare.
     * @param obj l'oggetto (serializzabile).
     * @return true se l'operazione � andata a buon fine, false altrimenti.
     * @throws Exception se � avvenuta un'eccezione durante la scrittura.
     */
	@Override
	public boolean writeInInternalStorage(String subFolder, String fileName,
			Serializable obj) throws Exception {
		String path = getStorageFilePath(subFolder, fileName, false);
		return write(path, obj);
	}

	/**
	 * Metodo di comodo per la lettura di un Oggetto da File in Memoria di Storage Interna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
	 * @param fileName il nome del file da cercare.
	 * @return l'Oggetto, se la lettura � andata a buon fine, null altrimenti.
	 */
	@Override
	public Serializable readFromInternalStorage(String subFolder, String fileName) {
		String path = getStorageFilePath(subFolder, fileName, false);
		return read(path);
	}

}
