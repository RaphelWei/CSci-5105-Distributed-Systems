service WorkWithNode {
	// bool ping(),
	// void Set(1: string Book_title, 2: string Genre),
	// string Get(1: string Book_title),
	string UpdateDHT(1:string SourceInfo),
	void UpdateFingerTable(1: string SourceInfo, 2:string affectKeyIndex),
	string find_successor_ByKey(1:string key),
	string find_predeccessor_ByKey(1:string key),
	void setfingerTable(1:string content),
	string getfingerTable()
}
