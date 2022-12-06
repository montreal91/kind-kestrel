
#include "Common.h"


namespace __util__ {
void read_from_file(const std::string& path, std::string* text) {
  std::ifstream in(path);
  in.seekg(0, std::ios::end);
  text->reserve(in.tellg());
  in.seekg(0, std::ios::beg);

  text->assign((std::istreambuf_iterator<char>(in)), std::istreambuf_iterator<char>());
}

} // namespace __util__
