package com.shockdom.download.fs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;

/**
 * Classe per la lettura/scrittura di File dal FileSystem, identificati da una stringa. E' possibile serializzare qualunque tipo di File a partire da un InputStream, aperto in lettura e passato come parametro ad ogni metodo di scrittura.
 * Tramite questa classe � possibile serializzare una risorsa in Cache (svuotabile dalle impostazioni), o in spazio di Storage (svuotabile disinstallando l'applicazione), o in una qualunque locazione di memoria, specificata mediante un path.
 * E' possibile inoltre decidere di archiviare la risorsa in Memoria Esterna (sdcard), Interna (/data/data), o delegare a questa classe la decisione.
 * 
 * @author Walter Verdese
 *
 */
public class FileStorageManager extends FileSystemStorageManager<String, InputStream, File> {

	/**
	 * Costruttore a visibilit� ridotta: � preferibile istanziare questo StorageManager come Singleton, in modo tale che questa classe costituisca un singolo entry point per la gestione del File System dell'intera applicazione.
	 */
	public FileStorageManager(Context c) {
		super(c);
	}

	/**
	 * Metodo per la scrittura di uno stream su File.
	 * Tutte le scritture implementate da questa classe invocano questo metodo per operare sul FileSystem.
	 * Eventuali invocazioni concorrenti di questo metodo vengono sincronizzate.
	 * 
	 * @param filePath il path assoluto del File da serializzare.
	 * @param stream uno stream aperto il lettura per la risorsa da serializzare.
	 * @return true se l'operazione di scrittura � andata a buon fine, false altrimenti.
	 * @throws Exception se � avvenuta un'eccezione durante la scrittura.
	 */
	public synchronized boolean write(String filePath, InputStream stream) throws Exception {	
		BufferedInputStream inStream = new BufferedInputStream(stream);
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
	 * Metodo per la lettura di un File.
	 * Tutte le letture implementate da questa classe invocano questo metodo per operare sul FileSystem.
	 * Eventuali invocazioni concorrenti di questo metodo vengono sincronizzate.
	 * 
	 * @param filePath il path assoluto del File.
	 * @return il risultato della lettura se l'operazione � andata a buon fine, null altrimenti.
	 */
	public synchronized File read(String filePath) {
		if (filePath != null) {
			File file = new File(filePath);
			if (file != null && file.exists()) {
				return file;
			}
		}
		return null;
	}	

	/**
	 * Metodo di comodo per la scrittura di un File in Memoria di Cache (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
	 * 
	 * @param inStream Uno stream aperto il lettura per la risorsa da serializzare.
	 * @return il nome del file serializzato (senza path).
	 * @throws Exception se � avvenuta un'eccezione durante la scrittura.
	 */
	public String writeInCache(InputStream inStream) throws Exception {
		boolean isOk = false;
		Exception e = null;
		//Log.i("FILE DEBUG", "Starting writing operation from stream.");
		String fileName = getFileNameFromURL(""+System.currentTimeMillis());
		try {
			//l'implementazione esegue una scrittura in cache AUT l'altra
			if (isExternalStorageSupported()) {
				isOk = writeInExternalCache(null, fileName, inStream);
			}
			else {
				isOk = writeInInternalStorage(null, fileName, inStream);
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
	 * Metodo di comodo per la scrittura di un File in Memoria di Cache (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
	 * 
	 * @param inStream Uno stream aperto il lettura per la risorsa da serializzare.
	 * @param fileName il nome del file da serializzare.
	 * @return true se l'operazione � andata a buon fine, false altrimenti.
	 * @throws Exception se � avvenuta un'eccezione durante la scrittura.
	 */
	public boolean writeInCache(String fileName, InputStream inStream) throws Exception {
		boolean isOk = false;
		Exception e = null;
		//Log.i("FILE DEBUG", "Starting writing operation from stream.");
		try {
			//l'implementazione esegue una scrittura in cache AUT l'altra
			if (isExternalStorageSupported()) {
				isOk = writeInExternalCache(null, fileName, inStream);
			}
			else {
				isOk = writeInInternalStorage(null, fileName, inStream);
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
	 * Metodo di comodo per la lettura di un File dalla Memoria di Cache (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
	 * 
	 * @param fileName il nome del file (senza path) contenuto in memoria di Cache da cercare.
	 * @return il File se la lettura � andata a buon fine, null altrimenti.
	 */
	public File readFromCache(String fileName) {
		File file = readFromExternalCache(null, fileName);
		if (file == null) {
			file = readFromInternalCache(null, fileName);
		}
		return file;
	}
	
	/**
	 * Metodo di comodo per la lettura di un File dalla Memoria di Cache (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Cache.
	 * @param fileName il nome del file (senza path) contenuto in memoria di Cache da cercare.
	 * @return il File se la lettura � andata a buon fine, null altrimenti.
	 */
	@Override
	public File readFromCache(String subfolder, String fileName) {
		File file = readFromExternalCache(subfolder, fileName);
		if (file == null) {
			file = readFromInternalCache(subfolder, fileName);
		}
		return file;
	}

	/**
	 * Metodo di comodo per la scrittura di un File in Memoria di Cache Esterna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Cache.
	 * @param fileName il nome del file da serializzare.
	 * @param stream uno stream aperto il lettura per la risorsa da serializzare.
	 * @return true se l'operazione � andata a buon fine, false altrimenti.
	 * @throws Exception se � avvenuta un'eccezione durante la scrittura.
	 */
	public boolean writeInExternalCache(String subFolder, String fileName, InputStream stream) throws Exception {
		String path = getCacheFilePath(subFolder, fileName, true);
		return write(path, stream);
	}

	/**
	 * Metodo di comodo per la lettura di un File in Memoria di Cache Esterna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Cache.
	 * @param fileName il nome del file da cercare.
	 * @return il File, se la lettura � andata a buon fine, null altrimenti.
	 */
	public File readFromExternalCache(String subFolder, String fileName) {
		String path = getCacheFilePath(subFolder, fileName, true);
		return read(path);
	}

	/**
	 * Metodo di comodo per la scrittura di un File in Memoria di Cache Interna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Cache.
	 * @param fileName il nome del file da serializzare.
	 * @param stream uno stream aperto il lettura per la risorsa da serializzare.
	 * @return true se l'operazione � andata a buon fine, false altrimenti.
	 * @throws Exception se � avvenuta un'eccezione durante la scrittura.
	 */
	public boolean writeInInternalCache(String subFolder, String fileName, InputStream stream) throws Exception {
		String path = getCacheFilePath(subFolder, fileName, false);
		return write(path, stream);
	}

	/**
	 * Metodo di comodo per la lettura di un File in Memoria di Cache Interna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Cache.
	 * @param fileName il nome del file da cercare.
	 * @return il File, se la lettura � andata a buon fine, null altrimenti.
	 */
	public File readFromInternalCache(String subFolder, String fileName) {
		String path = getCacheFilePath(subFolder, fileName, false);
		return read(path);
	}

	/**
	 * Metodo di comodo per la scrittura di un File in Memoria di Storage (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
	 * 
	 * @param inStream Uno stream aperto il lettura per la risorsa da serializzare.
	 * @return il nome del file serializzato (senza path).
	 * @throws Exception se � avvenuta un'eccezione durante la scrittura.
	 */
	public String writeInStorage(InputStream inStream) throws Exception {
		boolean isOk = false;
		Exception e = null;
		//Log.i("FILE DEBUG", "Starting writing operation from stream.");
		String fileName = getFileNameFromURL(""+System.currentTimeMillis());
		try {
			//l'implementazione esegue una scrittura in cache AUT l'altra
			if (isExternalStorageSupported()) {
				isOk = writeInExternalStorage(null, fileName, inStream);
			}
			else {
				isOk = writeInInternalStorage(null, fileName, inStream);
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
	 * Metodo di comodo per la scrittura di un File in Memoria di Storage (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
	 * 
	 * @param inStream Uno stream aperto il lettura per la risorsa da serializzare.
	 * @param fileName il nome del file serializzato (senza path).
	 * @return true se l'operazione � andata a buon fine, false altrimenti.
	 * @throws Exception se � avvenuta un'eccezione durante la scrittura.
	 */
	public boolean writeInStorage(String fileName, InputStream inStream) throws Exception {
		return writeInStorage(null, fileName, inStream);
	}

	/**
	 * Metodo di comodo per la scrittura di un File in Memoria di Storage (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
	 *
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
	 * @param inStream Uno stream aperto il lettura per la risorsa da serializzare.
	 * @param fileName il nome del file serializzato (senza path).
	 * @return true se l'operazione � andata a buon fine, false altrimenti.
	 * @throws Exception se � avvenuta un'eccezione durante la scrittura.
	 */
	public boolean writeInStorage(String subFolder, String fileName, InputStream inStream) throws Exception {
		boolean isOk = false;
		Exception e = null;
		//Log.i("FILE DEBUG", "Starting writing operation from stream.");
		try {
			//l'implementazione esegue una scrittura in cache AUT l'altra
			if (isExternalStorageSupported()) {
				isOk = writeInExternalStorage(subFolder, fileName, inStream);
			}
			else {
				isOk = writeInInternalStorage(subFolder, fileName, inStream);
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
	 * Metodo di comodo per la lettura di un File dalla Memoria di Storage (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
	 * 
	 * @param fileName il nome del file da serializzare.
	 * @return il File, se la lettura � andata a buon fine, null altrimenti.
	 */
	public File readFromStorage(String fileName) {
		File file = readFromExternalStorage(null, fileName);
		if (file == null) {
			file = readFromInternalStorage(null, fileName);
		}
		return file;
	}
	
	/**
	 * Metodo di comodo per la lettura di un File dalla Memoria di Storage (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
	 * @param fileName il nome del file da serializzare.
	 * @return il File, se la lettura � andata a buon fine, null altrimenti.
	 */
	@Override
	public File readFromStorage(String subfolder, String fileName) {
		File file = readFromExternalStorage(subfolder, fileName);
		if (file == null) {
			file = readFromInternalStorage(subfolder, fileName);
		}
		return file;
	}

	/**
	 * Metodo di comodo per la scrittura di un File in Memoria di Storage Esterna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
	 * @param fileName il nome del file da serializzare.
	 * @param stream uno stream aperto il lettura per la risorsa da serializzare.
	 * @return true se l'operazione � andata a buon fine, false altrimenti.
	 * @throws Exception se � avvenuta un'eccezione durante la scrittura.
	 */
	public boolean writeInExternalStorage(String subFolder, String fileName, InputStream stream) throws Exception {
		String path = getStorageFilePath(subFolder, fileName, true);
		return write(path, stream);
	}

	/**
	 * Metodo di comodo per la lettura di un File in Memoria di Storage Esterna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
	 * @param fileName il nome del file da cercare.
	 * @return il File, se la lettura � andata a buon fine, null altrimenti.
	 */
	public File readFromExternalStorage(String subFolder, String fileName) {
		String path = getStorageFilePath(subFolder, fileName, true);
		return read(path);
	}

	/**
	 * Metodo di comodo per la scrittura di un File in Memoria di Storage Interna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
	 * @param fileName il nome del file da serializzare.
	 * @param stream uno stream aperto il lettura per la risorsa da serializzare.
	 * @return true se l'operazione � andata a buon fine, false altrimenti.
	 * @throws Exception se � avvenuta un'eccezione durante la scrittura.
	 */
	public boolean writeInInternalStorage(String subFolder, String fileName, InputStream stream) throws Exception {
		String path = getStorageFilePath(subFolder, fileName, false);
		return write(path, stream);
	}

	/**
	 * Metodo di comodo per la lettura di un File in Memoria di Storage Interna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
	 * @param fileName il nome del file da cercare.
	 * @return il File, se la lettura � andata a buon fine, null altrimenti.
	 */
	public File readFromInternalStorage(String subFolder, String fileName) {
		String path = getStorageFilePath(subFolder, fileName, false);
		return read(path);
	}

}
