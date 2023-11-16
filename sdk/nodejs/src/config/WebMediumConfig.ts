

import { XMLWriter } from "../util/Utils";
import Config from "./Config";
abstract class WebMediumConfig extends Config {
    id?: string
    name?: string
    isAdmin?: boolean
    isAdult?: boolean
	isPrivate?: boolean
	isHidden?: boolean
	accessMode?: string
	isFlagged?: boolean
	isExternal?: boolean
	isPaphus?: boolean
	showAds: boolean = true
	forkAccessMode?: string
	contentRating?: string
	description?: string
	details?: string
	disclaimer?: string
	website?: string
	subdomain?: string
	tags?: string
	categories?: string
	flaggedReason?: string
	creator?: string
	creationDate?: string
	lastConnectedUser?: string
	license?: string
	avatar?: string
	script?: string
	graphic?: string
	thumbsUp:number = 0
	thumbsDown: number = 0
	stars:string = "0"
	connects?: string
	dailyConnects?: string
	weeklyConnects?: string
	monthlyConnects?: string
    abstract toXML(): string
    abstract getType(): string
    abstract credentials(): WebMediumConfig
    stats(): string {
        return ""
    }
    //TODO: displayCreationDate()

    getToken(): number{
        let token: number = 0
        if((this.token != undefined) && (this.token === "")) {
            token = Number(this.token)
        }
        return token
    }
    writeXML(writer:XMLWriter) {
        this.writeCredentials(writer)
        writer.appendAttribute("id", this.id)
        writer.appendAttribute("name", this.name)
        writer.appendAttribute("isPrivate", this.isPrivate)
        writer.appendAttribute("isHidden", this.isHidden)
        writer.appendAttribute("accessMode", this.accessMode)
        writer.appendAttribute("contentRating",this.contentRating)
        writer.appendAttribute("forkAccessMode", this.forkAccessMode)
        writer.appendAttribute("stars", this.stars)
        writer.appendAttribute("isAdult", this.isAdult)
        writer.appendAttribute("isFlagged", this.isFlagged)
        writer.appendAttribute("isExternal", this.isExternal)
        writer.appendAttribute("showAds", this.showAds)
        writer.append(">")
        writer.appendElement("description", this.description, true)
        writer.appendElement("details", this.details, true)
        writer.appendElement("disclaimer", this.disclaimer, true)
        writer.appendElement("categories", this.categories)
        writer.appendElement("tags", this.tags)
        writer.appendElement("license", this.license)
        writer.appendElement("website", this.website)
        writer.appendElement("subdomain", this.subdomain)
        writer.appendElement("flaggedReason", this.flaggedReason, true)
    }

    parseXML(element: any): void {
        
    }
}

export default WebMediumConfig