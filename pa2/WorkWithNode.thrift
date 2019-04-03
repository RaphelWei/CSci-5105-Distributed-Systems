service WorkWithNode {
	// bool ping(),
	void Set(1: string Book_title, 2: string Genre),
	string Get(1: string Book_title),
	string UpdateDHT(),
	void UpdateFingerTable(1: string SourceInfo, 2:string affectKeyIndex),
	string find_successor_ByKey(1:string key, 2:string initID, 3:bool passedZero),
	string find_predeccessor_ByKey(1:string key, 2:string initID, 3:bool passedZero)
}
