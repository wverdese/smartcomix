package com.shockdom.download.fs;

/**
 * Interfaccia da implementare se si vuole realizzare un modulo di lettura/scrittura su uno specifico supporto.
 * 
 * @author Walter Verdese
 *
 * @param <Identifier> il tipo generico dell'identificatore della risorsa da serializzare.
 * @param <InputType> il tipo generico della risorsa da serializzare.
 * @param <OutputType> il tipo generico del risultato della lettura.
 */
public interface StorageManagerInterface<Identifier, InputType, OutputType> {
	
	/**
	 * Metodo generico per la scrittura su un supporto.
	 * 
	 * @param id Identificatore univoco della risorsa da serializzare.
	 * @param input La risorsa da serializzare.
	 * @return true se l'operazione di scrittura � andata a buon fine, false altrimenti.
	 * @throws Exception se � avvenuta un'eccezione durante la scrittura.
	 */
	public boolean write(Identifier id, InputType input) throws Exception;
	
	/**
	 * Metodo generico per la lettura da un supporto.
	 * 
	 * @param id Identificatore univoco della risorsa da rintracciare.
	 * @return il risultato della lettura se l'operazione � andata a buon fine, null altrimenti.
	 */
	public OutputType read(Identifier id);
	
	/**
	 * Metodo generico per l'interruzione di un'operazione di scrittura.
	 */
	public void interrupt();

}
