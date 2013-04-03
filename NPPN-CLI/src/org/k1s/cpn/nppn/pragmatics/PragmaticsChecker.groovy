package org.k1s.cpn.nppn.pragmatics

import org.cpntools.accesscpn.model.Instance;
import org.cpntools.accesscpn.model.Place;
import org.cpntools.accesscpn.model.Page;
import org.cpntools.accesscpn.model.Transition;

class PragmaticsChecker {
	
	
	static boolean check(cpn, prags){
		def violations = []
		def retval = true
		
		def root = cpn.page[0]
		if(!PragmaticsChecker.checkPage(root, prags, violations, PageLevels.PROTOCOL)){
			retval = false
		}
		
		def  serviceSubsts = []
		
		root.object.findAll { it instanceof Instance}.each{ Instance subst ->
			def pageId = subst.subPageID
			def page = cpn.page.find{Page page -> pageId == page.id }
			
			if(!PragmaticsChecker.checkPage(page, prags, violations, PageLevels.PRINCIPAL)){
				retval = false
			}
			
			page.object.findAll { it instanceof Instance}.each  {serviceSubsts << it}
		}
		
		
		serviceSubsts.each{Instance subst ->
			def pageId = subst.subPageID
			def page = cpn.page.find{Page page -> pageId == page.id }
			
			if(!PragmaticsChecker.checkPage(page, prags, violations, PageLevels.SERVICE)){
				retval = false
			}
		}
		
		/*cpn.page.each{
			if(!PragmaticsChecker.checkPage(it, prags, violations)){
				retval = false
			}
		}*/
		//println "checl returning: $retval"
		return retval
	}
	
	
	static boolean checkPage(page, prags, violations, pageType){
		def retval = true
		page.object.each{
			if(it.pragmatics){
				if(!PragmaticsChecker.checkObject(it, prags, page, violations, pageType)){
					retval = false
				}
			}
		}
		//println "checkPage returning $retval"
		return retval
	}
	
	
	static boolean checkObject(obj, pragDescs, page, violations, pageType){
		def retval = true
		println "checking obj: $obj"
		obj.pragmatics.each { prag ->
			println "\tfor ${prag.name}"
			def desc = pragDescs[prag.name]
			println "\t\t constraints: ${desc == null ? 'nodesc' : desc.constraints}"
			if(desc && desc.constraints instanceof List){
				
			} else if(desc && desc.constraints instanceof PragmaticsConstraints ){
				//println "\t\t constraint ${desc.constraints}"
				if(desc.constraints.connectedTypes && desc.constraints.connectedTypes instanceof String){
					if(!PragmaticsChecker.checkConnectedType(desc.constraints.connectedTypes, obj)){
						println "not ok"
						retval = false
					} else{
						println "ok"
					}
					
					if(!PragmaticsChecker.checklevel(desc.constraints.levels, pageType)){
						println "not ok"
						retval = false
					} else{
						println "ok"
					}
				}
			}
		}
		//println "checkObj returning $retval"
		return retval
	}
	
	static boolean checklevel(levels, pageType){
		println "levels: $levels"
		println "levels: ${levels.class}"
		if(levels){
			return PageLevels.checkSame(levels, pageType)
		}
		return true
	}
	
	static boolean checkConnectedType(typeStr, obj){
		def retval = false
		switch(typeStr){
			case 'Place':
				return obj instanceof Place
				break
			case 'Transition':
				return  obj instanceof Transition
				break
			case 'SubstitutionTransition':
				//println "substTrans: ${obj.class}"
				//println "${obj instanceof Instance}"
				return (obj instanceof Instance)
				break
			default:
				throw new Exception("unknown node type: $typeStr")
		}
		return retval
	}
}

enum PageLevels{
	PROTOCOL,
	PRINCIPAL,
	SERVICE
	
	static boolean checkSame(levelName, type){
		switch(levelName){
			case "protocol":
				return type == PageLevels.PROTOCOL
			case "principal":
				return type == PageLevels.PRINCIPAL
			case "service":
				return type == PageLevels.SERVICE
			default:
				throw new Exception("unknown level name: ${levelName}")
		}
	}
}