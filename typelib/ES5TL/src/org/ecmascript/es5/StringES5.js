/**
 * 
 */
//> public
vjo.mtype('org.ecmascript.es5.StringES5')

.props({
	
})
.protos({
	/**
	 * Trims whitespace from both ends of the string
	 */
	//> public String trim()
	trim: vjo.NEEDS_IMPL,
	
	/**
	 * Trims whitespace from the right side of the string
	 */
	//> public String trimRight()
	trimRight: vjo.NEEDS_IMPL,
	
	/**
	 * Trims whitespace from the left side of the string
	 */
	//> public String trimLeft()
	trimLeft: vjo.NEEDS_IMPL
})
.endType();
