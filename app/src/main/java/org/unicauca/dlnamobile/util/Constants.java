package org.unicauca.dlnamobile.util;

public interface Constants {

	static final String HEADER_SLDEVICEID = "SLDeviceID";

	/**
	 * URI para el establecimiento de la conexion con al PC
	 */
	static final String CONNECTION_PATH = "/reproductor";

	/**
	 * URI para el envio de mensajes al PC
	 */
	static final String QUEUE_PATH = "/reproductor";


	/**
	 * URI para el LongPolling y permanecer atento a un nuevo mensaje para este
	 * dispotivo
	 */
	static final String POLLING_PATH = "/reproductor";

}
