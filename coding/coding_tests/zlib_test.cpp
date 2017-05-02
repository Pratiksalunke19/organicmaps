#include "testing/testing.hpp"

#include "coding/zlib.hpp"

#include "base/macros.hpp"
#include "base/string_utils.hpp"

#include "std/cstdint.hpp"
#include "std/iterator.hpp"
#include "std/sstream.hpp"
#include "std/string.hpp"
#include "std/utility.hpp"
#include "std/vector.hpp"

using namespace coding;

using Deflate = ZLib::Deflate;
using Inflate = ZLib::Inflate;

pair<Deflate::Format, Inflate::Format> const g_combinations[] = {
    {Deflate::Format::ZLib, Inflate::Format::ZLib},
    {Deflate::Format::ZLib, Inflate::Format::Both},
    {Deflate::Format::GZip, Inflate::Format::GZip},
    {Deflate::Format::GZip, Inflate::Format::Both}};

namespace
{
void TestDeflateInflate(string const & original)
{
  for (auto const & p : g_combinations)
  {
    Deflate const deflate(p.first /* format */, Deflate::Level::BestCompression);
    Inflate const inflate(p.second /* format */);

    string compressed;
    TEST(deflate(original, back_inserter(compressed)), ());

    string decompressed;
    TEST(inflate(compressed, back_inserter(decompressed)), ());

    TEST_EQUAL(original, decompressed, ());
  }
}

UNIT_TEST(ZLib_Smoke)
{
  Deflate const deflate(Deflate::Format::ZLib, Deflate::Level::BestCompression);
  Inflate const inflate(Inflate::Format::ZLib);

  {
    string s;
    TEST(!deflate(nullptr, 0, back_inserter(s)), ());
    TEST(!deflate(nullptr, 4, back_inserter(s)), ());
    TEST(!inflate(nullptr, 0, back_inserter(s)), ());
    TEST(!inflate(nullptr, 4, back_inserter(s)), ());
  }

  TestDeflateInflate("");
  TestDeflateInflate("Hello, World!");
}

UNIT_TEST(ZLib_Large)
{
  string original;
  for (size_t i = 0; i < 1000; ++i)
    original += strings::to_string(i);

  TestDeflateInflate(original);
}

UNIT_TEST(GZip_ForeignData)
{
  // To get this array of bytes, type following:
  //
  // echo -n 'Hello World!' | gzip -c | od -t x1
  uint8_t const data[] = {0x1f, 0x8b, 0x08, 0x08, 0x6d, 0x55, 0x08, 0x59, 0x00, 0x03, 0x73,
                          0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2e, 0x74, 0x78, 0x74, 0x00, 0xf3,
                          0x48, 0xcd, 0xc9, 0xc9, 0xd7, 0x51, 0x08, 0xcf, 0x2f, 0xca, 0x49,
                          0x51, 0x04, 0x00, 0xd0, 0xc3, 0x4a, 0xec, 0x0d, 0x00, 0x00, 0x00};

  string s;

  Inflate const inflate(Inflate::Format::GZip);
  TEST(inflate(data, ARRAY_SIZE(data), back_inserter(s)), ());
  TEST_EQUAL(s, "Hello, World!", ());
}
}  // namespace
