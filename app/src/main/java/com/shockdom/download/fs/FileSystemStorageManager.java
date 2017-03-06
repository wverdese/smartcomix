package com.shockdom.download.fs;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Implementazione astratta della StorageManagerInterface, che realizza parzialmente le funzionalit� di lettura/scrittura su file system di una risorsa.
 * Tramite questa classe � possibile serializzare una risorsa in Cache (svuotabile dalle impostazioni), o in spazio di Storage (svuotabile disinstallando l'applicazione), o in una qualunque locazione di memoria, specificata mediante un path.
 * E' possibile inoltre decidere di archiviare la risorsa in Memoria Esterna (sdcard), Interna (/data/data), o delegare a questa classe la decisione.
 * 
 * @author Walter Verdese
 *
 * @param <InputType> il tipo generico della risorsa da serializzare.
 * @param <OutputType> il tipo generico del risultato della lettura.
 */
public abstract class FileSystemStorageManager<Identifier, InputType, OutputType> implements StorageManagerInterface<Identifier, InputType, OutputType> {
	
	public static final String TAG = Class.class.getSimpleName();
	
	/** Valore possibile per il parametro subFolder, per i file di tipo Documento. **/
	public static final String DOC_FOLDER = "Documents";
	/** Valore possibile per il parametro subFolder, per i file di tipo Immagine. **/
	public static final String IMG_FOLDER = "Pictures";
	/** Valore possibile per il parametro subFolder, per i file di tipo Media. **/
	public static final String MEDIA_FOLDER = "Media";	
	/** Valore possibile per il parametro subFolder, per i file miscellanei. **/
	public static final String DWN_FOLDER = "Download";
	/** Valore possibile per il parametro subFolder, per i file temporanei. **/
	public static final String TMP_FOLDER = "Temp";
	
	private static final int BUFFER_SIZE = 2048; 
	private boolean isWriting = true;
	private boolean interrupted = false;
	
	protected Context context;
	
	protected FileSystemStorageManager(Context c) {
		context = c;
	}
	
	/**
	 * Controlla l'esistenza e la disponibilit� di un dispositivo di Memoria Esterna.
	 * 
	 * @return true � la memoria � disponibile, false altrimenti.
	 */
	public boolean isExternalStorageSupported() {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but all we need
			//  to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		boolean toret = false;
		if (mExternalStorageAvailable && mExternalStorageWriteable) {
			//check if Context manages to find the external cache folder
			return context.getExternalCacheDir() != null;
		}
		return toret;
	}
	
	/**
	 * Restituisce la directory di Cache dell'applicazione (Esterna se disponibile, Interna in caso contrario).
	 * 
	 * @return il File che punta alla directory di Cache se la lettura � andata a buon fine, null altrimenti.
	 */
	public File getCacheDirectory() {
		File storageDir = getExternalCacheDirectory(null);
		if (storageDir == null) {
			storageDir = getInternalCacheDirectory(null);
		}
		return storageDir;
	}
	
	/**
	 * Restituisce la directory di Cache su Memoria Esterna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Cache.
	 * @return il file che punta alla directory di Cache (o alla sottocartella richiesta) se la lettura � andata a buon fine, null altrimenti.
	 */
	public File getExternalCacheDirectory(String subFolder) {
		File storageDir = context.getExternalCacheDir();
		return getDirectory(storageDir, subFolder);
	}
	
	/**
	 * Restituisce la directory di Cache su Memoria Interna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Cache.
	 * @return il file che punta alla directory di Cache (o alla sottocartella richiesta) se la lettura � andata a buon fine, null altrimenti.
	 */
	public File getInternalCacheDirectory(String subFolder) {
		File storageDir = context.getCacheDir();
		return getDirectory(storageDir, subFolder);
	}
	
	/**
	 * Restituisce la directory di Storage dell'applicazione (esterna se disponibile, interna in caso contrario).
	 *
	 * @return il File che punta alla directory di Storage se la lettura � andata a buon fine, null altrimenti.
	 */
	public File getStorageDirectory() {
		return getStorageDirectory(null);
	}

	/**
	 * Restituisce la directory di Storage dell'applicazione (esterna se disponibile, interna in caso contrario).
	 *
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
	 * @return il File che punta alla directory di Storage se la lettura � andata a buon fine, null altrimenti.
	 */
	public File getStorageDirectory(String subFolder) {
		File storageDir = getExternalStorageDirectory(subFolder);
		if (storageDir == null) {
			storageDir = getInternalStorageDirectory(subFolder);
		}
		return storageDir;
	}
	
	/**
	 * Restituisce la directory di Storage su Memoria Esterna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
	 * @return il file che punta alla directory di Storage (o alla sottocartella richiesta) se la lettura � andata a buon fine, null altrimenti.
	 */
	public File getExternalStorageDirectory(String subFolder) {
		File storageDir = context.getExternalFilesDir(null);
		return getDirectory(storageDir, subFolder);
	}
	
	/**
	 * Restituisce la directory di Storage su Memoria Interna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
	 * @return il file che punta alla directory di Storage (o alla sottocartella richiesta) se la lettura � andata a buon fine, null altrimenti.
	 */
	public File getInternalStorageDirectory(String subFolder) {
		File storageDir = context.getFilesDir();
		return getDirectory(storageDir, subFolder);
	}
	
	/**
	 * Interrompe una qualsiasi operazione di scrittura. 
	 * In seguito all'interruzione eventuali file parzialmente scritti verranno eliminati.
	 */
	public void interrupt() {
		if (isWriting) {
			interrupted = true;
		}
	}
	
	/**
	 * Restituisce un identificatore testuale univoco per una risorsa, dato il suo URL.
	 * 
	 * @param url l'indirizzo della risorsa.
	 * @return un identificatore univoco.
	 */
    public String getFileNameFromURL(String url) {
    	String hash = null;
    	if (url != null) {
    		 MessageDigest digest;
    		    try {
    		        digest = MessageDigest.getInstance("MD5");
    		        byte utf8_bytes[] = url.getBytes();
    		        digest.update(utf8_bytes,0,utf8_bytes.length);
    		        hash = new BigInteger(1, digest.digest()).toString(16);
    		    } 
    		    catch (NoSuchAlgorithmException e) {
    		        e.printStackTrace();
    		    }
    	}
    	return hash;
    }
    
    /**
     * Metodo generico per la scrittura di una risorsa in Memoria di Cache (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
     * 
     * @param input la risorsa generica da serializzare
     * @return un identificatore univoco della risorsa serializzata.
     * @throws Exception se � avvenuta un'eccezione durante la scrittura.
     */
    public abstract Identifier writeInCache(InputType input) throws Exception;
    
    /**
     * Metodo generico per la scrittura di una risorsa in Memoria di Cache (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
     * 
     * @param input la risorsa generica da serializzare
     * @return un identificatore univoco della risorsa serializzata.
     * @throws Exception se � avvenuta un'eccezione durante la scrittura.
     */
    public abstract boolean writeInCache(Identifier id, InputType input) throws Exception;
    
    /**
     * Metodo generico per la lettura di una risorsa dalla Memoria di Cache (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
     * 
     * @param id identificatore univoco della risorsa serializzata.
     * @return la risorsa, se la lettura � andata a buon fine, null altrimenti.
     */
	public abstract OutputType readFromCache(Identifier id);
	
	/**
     * Metodo generico per la lettura di una risorsa dalla Memoria di Cache (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
     * 
     * @param subfolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Cache.
     * @param id identificatore univoco della risorsa serializzata.
     * @return la risorsa, se la lettura � andata a buon fine, null altrimenti.
     */
	public abstract OutputType readFromCache(String subfolder, Identifier id);
	
    /**
     * Metodo generico per la scrittura di una risorsa in Memoria di Cache Esterna.
     * 
     * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Cache.
     * @param id identificatore univoco della risorsa da serializzare.
     * @param input la risorsa generica da serializzare
     * @return true se l'operazione � andata a buon fine, false altrimenti.
     * @throws Exception se � avvenuta un'eccezione durante la scrittura.
     */
	public abstract boolean writeInExternalCache(String subFolder, Identifier id, InputType input) throws Exception;
	
	/**
	 * Metodo generico per la lettura di una risorsa in Memoria di Cache Esterna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Cache.
	 * @param id identificatore univoco della risorsa serializzata.
	 * @return la risorsa, se la lettura � andata a buon fine, null altrimenti.
	 */
	public abstract OutputType readFromExternalCache(String subFolder, Identifier id);
	
    /**
     * Metodo generico per la scrittura di una risorsa in Memoria di Cache Interna.
     * 
     * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Cache.
     * @param id identificatore univoco della risorsa da serializzare.
     * @param input la risorsa generica da serializzare
     * @return true se l'operazione � andata a buon fine, false altrimenti.
     * @throws Exception se � avvenuta un'eccezione durante la scrittura.
     */
	public abstract boolean writeInInternalCache(String subFolder, Identifier id, InputType input) throws Exception;
	
	/**
	 * Metodo generico per la lettura di una risorsa in Memoria di Cache Interna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Cache.
	 * @param id identificatore univoco della risorsa serializzata.
	 * @return la risorsa, se la lettura � andata a buon fine, null altrimenti.
	 */
	public abstract OutputType readFromInternalCache(String subFolder, Identifier id);
	
    /**
     * Metodo generico per la scrittura di una risorsa in Memoria di Storage (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
     * 
     * @param input la risorsa generica da serializzare
     * @return un identificatore univoco per la risorsa serializzata.
     * @throws Exception se � avvenuta un'eccezione durante la scrittura.
     */
	public abstract Identifier writeInStorage(InputType input) throws Exception;
	
    /**
     * Metodo generico per la scrittura di una risorsa in Memoria di Storage (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
     * 
     * @param input la risorsa generica da serializzare
     * @return un identificatore univoco per la risorsa serializzata.
     * @throws Exception se � avvenuta un'eccezione durante la scrittura.
     */
	public abstract boolean writeInStorage(Identifier id, InputType input) throws Exception;
	
    /**
     * Metodo generico per la lettura di una risorsa dalla Memoria di Storage (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
     * 
     * @param id identificatore univoco della risorsa serializzata.
     * @return la risorsa, se la lettura � andata a buon fine, null altrimenti.
     */
	public abstract OutputType readFromStorage(Identifier id);
	
	/**
     * Metodo generico per la lettura di una risorsa dalla Memoria di Storage (Esterna se disponibile, Interna altrimenti), senza sottocartelle.
     * 
     * @param subfolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
     * @param id identificatore univoco della risorsa serializzata.
     * @return la risorsa, se la lettura � andata a buon fine, null altrimenti.
     */
	public abstract OutputType readFromStorage(String subfolder, Identifier id);
	
    /**
     * Metodo generico per la scrittura di una risorsa in Memoria di Storage Esterna.
     * 
     * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
    * @param id identificatore univoco della risorsa da serializzare.
     * @param input la risorsa generica da serializzare
     * @return true se l'operazione � andata a buon fine, false altrimenti.
     * @throws Exception se � avvenuta un'eccezione durante la scrittura.
     */
	public abstract boolean writeInExternalStorage(String subFolder, Identifier id, InputType input) throws Exception;
	
	/**
	 * Metodo generico per la lettura di una risorsa in Memoria di Storage Esterna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
     * @param id identificatore univoco della risorsa serializzata.
	 * @return la risorsa, se la lettura � andata a buon fine, null altrimenti.
	 */
	public abstract OutputType readFromExternalStorage(String subFolder, Identifier id);
	
    /**
     * Metodo generico per la scrittura di una risorsa in Memoria di Storage Interna.
     * 
     * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
	 * @param id identificatore univoco della risorsa da serializzare.
     * @return true se l'operazione � andata a buon fine, false altrimenti.
     * @throws Exception se � avvenuta un'eccezione durante la scrittura.
     */
	public abstract boolean writeInInternalStorage(String subFolder, Identifier id, InputType input) throws Exception;
	
	/**
	 * Metodo generico per la lettura di una risorsa in Memoria di Storage Interna.
	 * 
	 * @param subFolder (opzionale) un path relativo di sottodirectory, a partire dalla directory di Storage.
     * @param id identificatore univoco della risorsa serializzata.
	 * @return la risorsa, se la lettura � andata a buon fine, null altrimenti.
	 */
	public abstract OutputType readFromInternalStorage(String subFolder, Identifier id);
    
	
	/**
	 * Metodo protetto per accedere una cartella. Se questa non esiste, il metodo la crea.
	 */
	protected File getDirectory(File storageDir, String subFolderName) {
		if (subFolderName != null) {
			storageDir = new File(storageDir, subFolderName);
		}
		if (storageDir != null && !storageDir.exists()) {
			storageDir.mkdirs();
		}
		//Log.i(TAG, "Storage dir: "+storageDir);
		return storageDir;
	}
	
	/**
	 * Metodo protetto per ottenere il path della directory di Cache (con subFolder opzionale): esterna se disponibile, interna in caso contrario.
	 */
	protected String getCacheFilePath(String subFolder, String fileName, boolean isExternal) {
		File storageDir = null;
		if (isExternal && isExternalStorageSupported()) {
			storageDir = getExternalCacheDirectory(subFolder);
		} else if (!isExternal) {
			storageDir = getInternalCacheDirectory(subFolder);
		}
		String path = null;
		if (storageDir != null && fileName != null) {
			path = new File(storageDir, fileName).getAbsolutePath();
		}
		return path;
	}
	
	/**
	 * Metodo protetto per ottenere il path della directory di Storage (con subFolder opzionale): esterna se disponibile, interna in caso contrario.
	 */
	protected String getStorageFilePath(String subFolder, String fileName, boolean isExternal) {
		File storageDir = null;
		if (isExternal && isExternalStorageSupported()) {
			storageDir = getExternalStorageDirectory(subFolder);
		} else if (!isExternal) {
			storageDir = getInternalStorageDirectory(subFolder);
		}
		String path = null;
		if (storageDir != null && fileName != null) {
			path = new File(storageDir, fileName).getAbsolutePath();
		}
		return path;
	}
	
	/**
	 * Metodo protetto per serializzare uno stream di input in uno stream di output, passati entrambi come parametro.
	 * Restituisce un feedback sul risultato dell'operazione.
	 */
	protected boolean write(InputStream inStream, OutputStream outStream) throws Exception {
		byte[] buffer = new byte[BUFFER_SIZE];
		int len = 0;
		boolean toret = false;
		isWriting = true;
		Exception e = null;
		
		//Log.i("FILE DEBUG", "Start writing loop.");
		try {
			while ((len = inStream.read(buffer)) > 0 ) {
				if (interrupted) {
					//Log.i("FILE DEBUG", "Writing loop has been interrupted.");
					interrupted = false;
					throw new InterruptedWritingOperationException("Forced interruption of writing operation.");
				}
				outStream.write(buffer, 0, len);
			}
			toret = true;
		} catch (Exception e1) {
			e = e1;
		}
		if (outStream != null) {
			outStream.flush();
			outStream.close();
		}		
		isWriting = false;
		if (e != null) {
			throw e;
		}
		return toret;
	}
	
	/**
	 * Classe protetta che realizza l'eccezione sollevata in caso di interruzione di scrittura, richiesta mediante il metodo interrupt().
	 */
	private static class InterruptedWritingOperationException extends Exception {

		private static final long serialVersionUID = -4954126989722303141L;

		public InterruptedWritingOperationException(String message) {
			super(message);
		}
		
	}

}
