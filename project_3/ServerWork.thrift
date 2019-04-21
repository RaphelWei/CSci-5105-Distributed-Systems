struct Node {
    // 1: i32 ID,
    1: string IP,
    2: string Port
}

struct REQ {
	1: string OP,
	2: string Filename,
	3: string Content,
	4: string ClientIP,
	5: string ClientPort
}

service ServerWork {
  void request(1: REQ r),
  i32 getVersion(1: string filename),
  string readback(1: REQ r),
  string writeback(1: REQ r),
  string overWriteFile(1: REQ r, 2: i32 NewestVerNum)
}
