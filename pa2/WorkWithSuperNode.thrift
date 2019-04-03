service WorkWithSuperNode {
	// bool ping(),
	// void setNumNode(1:i32 n),
	string Join(1:string IP, 2:string Port),
	void PostJoin(1:string IP, 2:string Port)
	// string GetNode()
	// string CheckFile(1: string path),
	// string WriteString(1:string fileName, 2:double poswords, 3:double negwords, 4:double sentiment),
	// list<string> getSentimentWords(1: string path),
	// string SortIntermediateData(1: map<string, string> statusRecords)
}
